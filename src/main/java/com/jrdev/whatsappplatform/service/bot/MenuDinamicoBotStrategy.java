package com.jrdev.whatsappplatform.service.bot;

import com.jrdev.whatsappplatform.dto.NotificacionMenuRequestDto;
import com.jrdev.whatsappplatform.model.*;
import com.jrdev.whatsappplatform.service.EvolutionClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MenuDinamicoBotStrategy implements BotStrategy {

    private final EvolutionClient evolutionClient;
    private final RestClient restClient;

    @Override
    public boolean soporta(String tipoIntegracion) {
        return "MENU_DINAMICO".equalsIgnoreCase(tipoIntegracion);
    }

    @Override
    public void procesarMensaje(WhatsappInstancia instancia, Contacto contacto, Mensaje mensaje, Integracion integracion) {
        String texto = mensaje.getContenido().trim();
        Map<String, Object> config = integracion.getConfiguration();
        String respuesta;

        if (config != null && !config.isEmpty()) {
            if (config.containsKey(texto)) {

                String nombreOpcion = config.get(texto).toString();
                respuesta = "Has seleccionado: *" + nombreOpcion + "*. En un momento te atenderemos.";

                // Si la empresa registró un webhook, le notificamos la selección
                if (integracion.getBaseUrl() != null && !integracion.getBaseUrl().isEmpty()) {
                    try {
                        NotificacionMenuRequestDto notificacion = NotificacionMenuRequestDto.builder()
                                .numeroTelefono(contacto.getNumeroTelefono())
                                .nombreCliente(contacto.getNombre())
                                .opcionSeleccionada(texto)
                                .etiquetaOpcion(nombreOpcion)
                                .idMensajeWhatsApp(mensaje.getEvolutionMessageId())
                                .build();

                        restClient.post()
                                .uri(integracion.getBaseUrl() + "/api/v1/whatsapp/webhook/menu")
                                .body(notificacion)
                                .retrieve()
                                .toBodilessEntity(); // No necesitamos mapear respuesta, solo avisar
                    } catch (Exception e) {
                        System.err.println("No se pudo notificar al webhook del menú dinámico: " + e.getMessage());
                    }
                }
            } else {
                // Armado del menú desde el JSONB
                StringBuilder menu = new StringBuilder();
                menu.append(config.getOrDefault("mensaje_bienvenida", "Por favor, elige una opción:\n\n"));

                config.forEach((key, value) -> {
                    if (key.matches("\\d+")) {
                        menu.append(key).append(". ").append(value).append("\n");
                    }
                });
                respuesta = menu.toString();
            }
        } else {
            respuesta = "El menú aún no ha sido configurado por el administrador.";
        }

        evolutionClient.enviarMensaje(
                instancia.getInstanceName(),
                contacto.getRemotejid().replace("@s.whatsapp.net", ""),
                respuesta
        );
    }
}