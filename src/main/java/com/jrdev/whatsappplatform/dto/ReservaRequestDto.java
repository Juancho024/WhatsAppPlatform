package com.jrdev.whatsappplatform.dto;

import lombok.Builder;
import lombok.Data;

// Aquí usamos @Builder de Lombok para que sea más fácil armarlo en tu BotStrategy
@Data
@Builder
public class ReservaRequestDto {
    private String numeroTelefono;
    private String nombreCliente;
    private String idTurnoSeleccionado;
    private String notas; // Ej: El mensaje original del cliente
}