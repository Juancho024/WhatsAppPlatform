package com.jrdev.whatsappplatform.service.bot;

import com.jrdev.whatsappplatform.dto.CrearTicketRequestDto;
import com.jrdev.whatsappplatform.dto.TicketUniversalResponseDto;
import com.jrdev.whatsappplatform.model.*;
import com.jrdev.whatsappplatform.service.EvolutionClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SoporteBotStrategy implements BotStrategy {

    private final EvolutionClient evolutionClient;
    private final RestClient restClient;

    @Override
    public boolean soporta(String tipoIntegracion) {
        return "SOPORTE".equalsIgnoreCase(tipoIntegracion);
    }

    @Override
    public void procesarMensaje(WhatsappInstancia instancia, Contacto contacto, Mensaje mensaje, Integracion integracion) {
        String texto = mensaje.getContenido().toLowerCase();
        String respuesta;

        if (texto.contains("falla") || texto.contains("error") || texto.contains("problema")) {
            try {
                String urlTickets = integracion.getBaseUrl() + "/api/v1/whatsapp/tickets";

                // 1. Armamos la petición de forma elegante usando el Builder
                CrearTicketRequestDto peticion = CrearTicketRequestDto.builder()
                        .numeroTelefono(contacto.getNumeroTelefono())
                        .nombreCliente(contacto.getNombre())
                        .descripcionFalla(mensaje.getContenido())
                        .fechaReporte(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .build();

                // 2. Ejecutamos el POST
                TicketUniversalResponseDto ticketData = restClient.post()
                        .uri(urlTickets)
                        .body(peticion)
                        .retrieve()
                        .body(TicketUniversalResponseDto.class);

                if (ticketData != null && ticketData.isExito()) {
                    respuesta = "Lamento los inconvenientes. 🛠️ He generado tu reporte con el número: *" + ticketData.getNumeroTicket() + "*. \n" +
                            "Estado actual: " + ticketData.getEstado() + "\n" +
                            "Nota: " + ticketData.getMensaje();
                } else {
                    respuesta = "No se pudo generar el ticket en el sistema central: " + (ticketData != null ? ticketData.getMensaje() : "Fallo de conexión.");
                }
            } catch (Exception e) {
                System.err.println("Error creando ticket externo: " + e.getMessage());
                respuesta = "Tuvimos un problema registrando tu falla en el sistema central. Por favor, aguarda un momento, un operador humano te asistirá pronto.";
            }
        } else {
            respuesta = "Estás comunicado con Soporte Técnico. Cuéntame en un solo mensaje cuál es el problema que presenta tu equipo (usa palabras como 'falla' o 'error').";
        }

        evolutionClient.enviarMensaje(
                instancia.getInstanceName(),
                contacto.getRemotejid().replace("@s.whatsapp.net", ""),
                respuesta
        );
    }
}