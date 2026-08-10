package com.jrdev.whatsappplatform.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TicketUniversalResponseDto {
    private boolean exito;
    private String mensaje;
    private String numeroTicket;
    private String estado;
}