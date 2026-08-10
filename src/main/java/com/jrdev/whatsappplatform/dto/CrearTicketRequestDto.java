package com.jrdev.whatsappplatform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrearTicketRequestDto {
    private String numeroTelefono;
    private String nombreCliente;
    private String descripcionFalla;
    private String fechaReporte; // En formato ISO (ej. 2026-08-09T18:30:00Z)
}