package com.jrdev.whatsappplatform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificacionMenuRequestDto {
    private String numeroTelefono;
    private String nombreCliente;
    private String opcionSeleccionada; // Ej: "1"
    private String etiquetaOpcion; // Ej: "Ventas Institucionales"
    private String idMensajeWhatsApp; // Por si necesitan rastrear el mensaje exacto
}