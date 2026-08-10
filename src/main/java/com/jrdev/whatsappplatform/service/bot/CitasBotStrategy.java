package com.jrdev.whatsappplatform.service.bot;

import com.jrdev.whatsappplatform.dto.DisponibilidadUniversalResponseDto;
import com.jrdev.whatsappplatform.dto.ReservaRequestDto;
import com.jrdev.whatsappplatform.dto.RespuestaSimpleUniversalDto;
import com.jrdev.whatsappplatform.model.*;
import com.jrdev.whatsappplatform.service.EvolutionClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class CitasBotStrategy implements BotStrategy {

    private final EvolutionClient evolutionClient;
    private final RestClient restClient;

    @Override
    public boolean soporta(String tipoIntegracion) {
        return "CITAS".equalsIgnoreCase(tipoIntegracion);
    }

    @Override
    public void procesarMensaje(WhatsappInstancia instancia, Contacto contacto, Mensaje mensaje, Integracion integracion) {
        String texto = mensaje.getContenido().toLowerCase();
        String respuesta;

        // CAMINO 1: El usuario quiere saber qué horarios hay disponibles (Consulta GET)
        if (texto.contains("horario") || texto.contains("mañana") || texto.contains("fecha") || texto.contains("disponible")) {
            try {
                String urlDisponibilidad = integracion.getBaseUrl() + "/api/v1/whatsapp/citas/disponibles";

                DisponibilidadUniversalResponseDto disponibilidad = restClient.get()
                        .uri(urlDisponibilidad)
                        .retrieve()
                        .body(DisponibilidadUniversalResponseDto.class);

                if (disponibilidad != null && disponibilidad.isExito() && disponibilidad.getTurnos() != null) {
                    StringBuilder sb = new StringBuilder("📅 *Horarios disponibles:*\n\n");
                    for (DisponibilidadUniversalResponseDto.TurnoDisponible turno : disponibilidad.getTurnos()) {
                        sb.append("🔹 *").append(turno.getFechaHora()).append("*\n");
                        sb.append("   Especialista: ").append(turno.getEspecialista()).append("\n");
                        sb.append("   Para agendar escribe: *reservar ").append(turno.getIdTurno()).append("*\n\n");
                    }
                    respuesta = sb.toString();
                } else {
                    respuesta = "No hay horarios disponibles en este momento.";
                }
            } catch (Exception e) {
                System.err.println("Error consultando disponibilidad: " + e.getMessage());
                respuesta = "En este momento no puedo acceder al calendario. Intenta de nuevo más tarde.";
            }

            // CAMINO 2: El usuario ya eligió un turno y quiere agendarlo (Petición POST)
        } else if (texto.contains("reservar") || texto.contains("agendar")) {
            try {
                // NOTA: Aquí asumo que extraes el ID del turno del texto del usuario (Ej: "reservar TRN-001")
                // Por ahora usamos un placeholder para que no falle.
                String turnoSeleccionado = "TRN-001";

                ReservaRequestDto peticion = ReservaRequestDto.builder()
                        .numeroTelefono(contacto.getNumeroTelefono())
                        .nombreCliente(contacto.getNombre())
                        .idTurnoSeleccionado(turnoSeleccionado)
                        .notas(mensaje.getContenido())
                        .build();

                RespuestaSimpleUniversalDto confirmacion = restClient.post()
                        .uri(integracion.getBaseUrl() + "/api/v1/whatsapp/reservar")
                        .body(peticion)
                        .retrieve()
                        .body(RespuestaSimpleUniversalDto.class);

                if (confirmacion != null && confirmacion.isExito()) {
                    respuesta = "✅ ¡Listo! Tu cita ha sido confirmada. " + confirmacion.getMensaje();
                } else {
                    respuesta = "❌ Hubo un problema agendando tu cita: " + (confirmacion != null ? confirmacion.getMensaje() : "Error del servidor externo.");
                }
            } catch (Exception e) {
                System.err.println("Error enviando reserva: " + e.getMessage());
                respuesta = "No pudimos procesar tu reserva por un problema de conexión con el sistema central.";
            }

            // CAMINO 3: Saludo inicial o mensaje no reconocido
        } else if (texto.contains("hola") || texto.contains("cita")) {
            respuesta = "¡Hola " + contacto.getNombre() + "! Bienvenido a nuestro sistema de reservas. 📅 ¿Para qué día te gustaría consultar horarios? (Escribe 'horarios' o 'mañana').";
        } else {
            respuesta = "No te entendí bien. Escribe 'horarios' para ver turnos disponibles o 'reservar' si ya sabes qué turno quieres.";
        }

        // Despachamos la respuesta finalmente armada
        evolutionClient.enviarMensaje(
                instancia.getInstanceName(),
                contacto.getRemotejid().replace("@s.whatsapp.net", ""),
                respuesta
        );
    }
}