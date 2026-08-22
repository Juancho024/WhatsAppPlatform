package com.jrdev.whatsappplatform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    // 🔥 Lee la URL dependiendo de dónde esté corriendo el backend
    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void enviarInvitacion(String destino, String nombreEmpresa, String token) {
        try {
            // 1. Usamos MimeMessage para poder enviar HTML
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(destino);
            helper.setSubject("🚀 Invitación para unirte a " + nombreEmpresa);

            // 2. Armamos el link dinámicamente
            String linkAceptar = frontendUrl + "/aceptar-invitacion?token=" + token;

            // 3. Plantilla HTML profesional (Inline CSS para que funcione en todos los correos)
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
                    + "<hr style='border: none; border-top: 1px solid #e2e8f0; margin-bottom: 20px;' />"
                    + "<p style='color: #94a3b8; font-size: 13px; text-align: center; margin: 0;'>"
                    + "Si el botón no funciona, copia y pega este enlace en tu navegador:<br>"
                    + "<a href='" + linkAceptar + "' style='color: #047857; word-break: break-all;'>" + linkAceptar + "</a>"
                    + "</p>"
                    + "<p style='color: #94a3b8; font-size: 13px; text-align: center; margin-top: 20px;'>"
                    + "Saludos cordiales,<br><strong>Juan Rijo</strong><br>Equipo de WhatsApp Platform"
                    + "</p>"
                    + "</div>"
                    + "</div>";

            // 4. Setear el HTML al correo (el 'true' indica que el contenido es HTML)
            helper.setText(htmlMsg, true);

            // 5. Enviar
            mailSender.send(mensaje);

        } catch (Exception e) {
            System.err.println("Error al enviar el correo HTML: " + e.getMessage());
            // 🔥 ESTA ES LA LÍNEA NUEVA: Le decimos al sistema que aborte la operación
            throw new RuntimeException("No se pudo conectar al servidor de correos.");
        }
    }
}