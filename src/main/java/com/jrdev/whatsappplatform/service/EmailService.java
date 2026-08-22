package com.jrdev.whatsappplatform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // 🔥 Ahora usamos la llave de Resend desde tus variables de entorno de Render
    @Value("${RESEND_API_KEY}")
    private String resendApiKey;

    public void enviarInvitacion(String destino, String nombreEmpresa, String token) {
        try {
            String linkAceptar = frontendUrl + "/aceptar-invitacion?token=" + token;

            // El mismo diseño bonito que armamos antes
            String htmlMsg = "<div style='font-family: Arial, sans-serif; background-color: #f8fafc; padding: 40px 0;'>"
                    + "<div style='max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; padding: 30px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);'>"
                    + "<div style='text-align: center; margin-bottom: 24px;'>"
                    + "<div style='width: 48px; height: 48px; background-color: #047857; color: white; border-radius: 10px; display: inline-flex; align-items: center; justify-content: center; font-size: 24px; font-weight: bold;'>WP</div>"
                    + "</div>"
                    + "<h2 style='color: #0f172a; text-align: center; font-size: 22px; margin-bottom: 16px;'>¡Te han invitado!</h2>"
                    + "<p style='color: #475569; font-size: 16px; line-height: 1.5; text-align: center; margin-bottom: 30px;'>"
                    + "Has sido invitado para unirte al equipo de <strong>" + nombreEmpresa + "</strong> en WhatsApp Platform. Únete para empezar a colaborar."
                    + "</p>"
                    + "<div style='text-align: center; margin-bottom: 30px;'>"
                    + "<a href='" + linkAceptar + "' style='background-color: #047857; color: #ffffff; padding: 12px 28px; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px; display: inline-block;'>Aceptar Invitación</a>"
                    + "</div>"
                    + "</div>"
                    + "</div>";

            // Armamos el JSON que Resend está esperando
            Map<String, Object> body = new HashMap<>();
            body.put("from", "WhatsApp Platform <onboarding@resend.dev>");
            body.put("to", List.of(destino));
            body.put("subject", "🚀 Invitación para unirte a " + nombreEmpresa);
            body.put("html", htmlMsg);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(body);

            // Disparamos la petición HTTP directamente (¡Esto salta el bloqueo de Render!)
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                System.err.println("Resend devolvió error: " + response.body());
                throw new RuntimeException("Error en la API de correos.");
            }

        } catch (Exception e) {
            System.err.println("Error crítico al enviar el correo API: " + e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo de invitación.");
        }
    }
}