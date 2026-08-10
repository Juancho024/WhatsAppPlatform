package com.jrdev.whatsappplatform.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DisponibilidadUniversalResponseDto {

    private boolean exito;
    private String mensaje;
    private List<TurnoDisponible> turnos;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TurnoDisponible {
        private String idTurno; // Ej: "TRN-001"
        private String fechaHora; // Ej: "2026-08-15 14:00"
        private String especialista; // Ej: "Dr. Pérez" o "Mecánico Juan"
    }
}