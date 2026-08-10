package com.jrdev.whatsappplatform.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespuestaSimpleUniversalDto {
    private boolean exito;
    private String mensaje; // Ej: "Recibido correctamente" o "Error guardando en BD"
}