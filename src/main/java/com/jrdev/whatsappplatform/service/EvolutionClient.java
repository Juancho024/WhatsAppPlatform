package com.jrdev.whatsappplatform.service;

import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class EvolutionClient {

    private final HttpClient client;
    private final ObjectMapper objectMapper;

    @Value("${evolution.url}")
    private String url;

    @Value("${evolution.apikey}")
    private String apiKey;

    // Inyectamos el ObjectMapper que ya tienes en Spring
    public EvolutionClient(HttpClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public String crearInstancia(String instanceName) {
        String endpoint = url + "/instance/create";
        try {
            Map<String, Object> payload = Map.of(
                    "instanceName", instanceName,
                    "integration", "WHATSAPP-BAILEYS",
                    "qrcode", true
            );
            String json = objectMapper.writeValueAsString(payload);
            String respuesta = ejecutarPost(endpoint, json);
            configurarWebhook(instanceName);

            return respuesta;
        } catch (Exception e) {
            throw new RuntimeException("Error creando el JSON para crear instancia", e);
        }
    }

    public String conectar(String instanceName) {
        String endpoint = url + "/instance/connect/" + instanceName;
        return ejecutarGet(endpoint);
    }

    public String estadoConexion(String instanceName) {
        String endpoint = url + "/instance/connectionState/" + instanceName;
        return ejecutarGet(endpoint);
    }

    public String desconectar(String instanceName) {
        String endpoint = url + "/instance/logout/" + instanceName;
        return ejecutarDelete(endpoint);
    }

    public void enviarMensaje(String instanceName, String numero, String texto) {
        String endpoint = url + "/message/sendText/" + instanceName;
        try {
            // ObjectMapper se encarga de escapar cualquier carácter especial en el texto
            Map<String, String> payload = Map.of(
                    "number", numero,
                    "text", texto
            );
            String json = objectMapper.writeValueAsString(payload);

            String respuesta = ejecutarPost(endpoint, json);
            System.out.println("✅ Mensaje enviado exitosamente a " + numero);

        } catch (Exception e) {
            System.err.println("❌ Error enviando mensaje a " + numero + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String ejecutarGet(String endpoint) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("apikey", apiKey)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            validarRespuesta(response);
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Error comunicando con Evolution API en GET", e);
        }
    }

    private String ejecutarPost(String endpoint, String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("apikey", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            validarRespuesta(response);
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Error en POST. Detalles: " + e.getMessage(), e);
        }
    }

    private String ejecutarDelete(String endpoint) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("apikey", apiKey)
                    .DELETE()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            validarRespuesta(response);
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Error comunicando con Evolution API en DELETE", e);
        }
    }

    private void validarRespuesta(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Evolution API respondió " + response.statusCode() + ": " + response.body());
        }
    }

    public void configurarWebhook(String instanceName) {
        String endpoint = url + "/webhook/set/" + instanceName;
        try {
            // Agregamos "enabled": true, que era lo que Evolution estaba pidiendo a gritos
            Map<String, Object> webhookData = Map.of(
                    "enabled", true,
                    "url", "http://host.docker.internal:8081/api/webhooks/evolution",
                    "webhookByEvents", false,
                    "webhookBase64", false,
                    "events", new String[]{"MESSAGES_UPSERT"}
            );

            Map<String, Object> payload = Map.of("webhook", webhookData);
            String json = objectMapper.writeValueAsString(payload);

            // Hacemos la petición directa para capturar la respuesta exacta
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("apikey", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("✅ Webhook automatizado configurado para la instancia: " + instanceName);
            } else {
                System.err.println("❌ Evolution rechazó el Webhook. Código: " + response.statusCode());
                System.err.println("❌ Respuesta de Evolution: " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Error interno de Java configurando webhook: " + e.getMessage());
        }
    }
}