package com.jrdev.whatsappplatform.service.bot;

import com.jrdev.whatsappplatform.dto.InventarioUniversalResponseDto;
import com.jrdev.whatsappplatform.model.*;
import com.jrdev.whatsappplatform.service.EvolutionClient;
import com.jrdev.whatsappplatform.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class VentasBotStrategy implements BotStrategy {

    private final EvolutionClient evolutionClient;
    private final RestClient restClient;
    private final SessionService sessionService; // 1. INYECTAMOS TU SERVICIO DE REDIS

    @Override
    public boolean soporta(String tipoIntegracion) {
        return "VENTAS".equalsIgnoreCase(tipoIntegracion);
    }

    @Override
    public void procesarMensaje(WhatsappInstancia instancia, Contacto contacto, Mensaje mensaje, Integracion integracion) {
        String texto = mensaje.getContenido().trim().toLowerCase();
        String instanceName = instancia.getInstanceName();
        String phoneNumber = contacto.getRemotejid().replace("@s.whatsapp.net", "");
        String respuesta = "";

        // 2. BUSCAMOS EN QUÉ ESTADO ESTÁ EL USUARIO EN REDIS
        String estadoActual = sessionService.getUserState(instanceName, phoneNumber);

        // 3. LA MÁQUINA DE ESTADOS (Director del flujo)
        switch (estadoActual) {
            case "NUEVO_CHAT":
                respuesta = "¡Hola " + contacto.getNombre() + "! Bienvenido al departamento de Ventas. 🛒\n\n" +
                        "Responde con el *NÚMERO* de la opción que deseas:\n" +
                        "1️⃣ Ver catálogo completo\n" +
                        "2️⃣ Consultar stock de productos\n" +
                        "3️⃣ Salir";

                // Guardamos que ahora está esperando una opción del menú
                sessionService.saveUserState(instanceName, phoneNumber, "ESPERANDO_OPCION");
                break;

            case "ESPERANDO_OPCION":
                if (texto.equals("1")) {
                    respuesta = "Puedes ver nuestro catálogo completo y comprar aquí: " + integracion.getBaseUrl();
                    sessionService.clearSession(instanceName, phoneNumber); // Limpiamos sesión porque ya terminó

                } else if (texto.equals("2")) {
                    evolutionClient.enviarMensaje(instanceName, phoneNumber, "Consultando nuestro inventario en tiempo real... ⏳");

                    respuesta = consultarInventario(integracion); //consultar db
                    sessionService.clearSession(instanceName, phoneNumber);

                } else if (texto.equals("3")) {
                    respuesta = "¡Gracias por contactarnos! Escríbenos cuando nos necesites.";
                    sessionService.clearSession(instanceName, phoneNumber); // Limpiamos sesión

                } else {
                    respuesta = "❌ Opción no válida. Por favor responde con 1, 2 o 3.";
                }
                break;

            default:
                sessionService.clearSession(instanceName, phoneNumber);
                respuesta = "Tu sesión ha expirado o hubo un error. Escribe cualquier cosa para volver a empezar.";
                break;
        }

        // 4. ENVIAMOS LA RESPUESTA FINAL
        if (!respuesta.isEmpty()) {
            evolutionClient.enviarMensaje(instanceName, phoneNumber, respuesta);
        }
    }

    // 5. TU LÓGICA DE LA API EXTRAÍDA A UN MÉTODO LIMPIO
    private String consultarInventario(Integracion integracion) {
        try {
            // 1. Usamos la URL exacta que pusimos en la base de datos
            String urlConsulta = integracion.getBaseUrl();

            // 2. Extraemos el token usando un Map nativo para evitar el error de LinkedHashMap
            Map<String, Object> configuracion = (Map<String, Object>) integracion.getConfiguration();
            String token = (configuracion != null && configuracion.containsKey("api_token"))
                    ? configuracion.get("api_token").toString()
                    : "";

            // 3. Petición HTTP lista para consumir Supabase (o cualquier otra API)
            InventarioUniversalResponseDto respuestaExterna = restClient.get()
                    .uri(urlConsulta)
                    .header("Authorization", "Bearer " + token)
                    .header("apikey", token) // 🔥 Supabase necesita este header extra
                    .retrieve()
                    .body(InventarioUniversalResponseDto.class);

            if (respuestaExterna != null && respuestaExterna.isExito()) {
                if (respuestaExterna.getDatos() == null || respuestaExterna.getDatos().isEmpty()) {
                    return "No encontré productos disponibles en este momento.";
                } else {
                    StringBuilder sb = new StringBuilder("✅ *Inventario Actualizado:*\n\n");
                    for (InventarioUniversalResponseDto.ProductoItem item : respuestaExterna.getDatos()) {
                        sb.append("📦 *").append(item.getNombre()).append("*\n");
                        sb.append("💵 Precio: $").append(item.getPrecio()).append("\n");
                        sb.append("📦 Stock: ").append(item.getStock()).append(" unidades\n");;
                        sb.append("📊 Estado: ").append(item.isDisponible() ? "En Stock" : "Agotado").append("\n\n");
                    }
                    return sb.toString();
                }
            } else {
                return "El sistema reportó: " + (respuestaExterna != null ? respuestaExterna.getMensaje() : "Error desconocido.");
            }
        } catch (Exception e) {
            System.err.println("Error consultando la API de la empresa " + integracion.getIdEmpresa() + ": " + e.getMessage());
            return "En este momento el sistema de inventario está en mantenimiento. Intenta en unos minutos.";
        }
    }
}