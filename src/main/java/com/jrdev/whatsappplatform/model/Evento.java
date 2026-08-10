package com.jrdev.whatsappplatform.model;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
public class Evento {
    private Long idEvento;
    private Long idWhatsAppInstancia;
    private String tipoEvento;
    private String external_event_id;
    private Map<String, Object> payload;
    private String estado;
    private String error;
    private OffsetDateTime receivedAt;
    private OffsetDateTime processedAt;

    public Evento() {
    }

    public Evento(Long idEvento, Long idWhatsAppInstancia, String tipoEvento, String external_event_id, Map<String, Object> payload, String estado, String error, OffsetDateTime receivedAt, OffsetDateTime processedAt) {
        this.idEvento = idEvento;
        this.idWhatsAppInstancia = idWhatsAppInstancia;
        this.tipoEvento = tipoEvento;
        this.external_event_id = external_event_id;
        this.payload = payload;
        this.estado = estado;
        this.error = error;
        this.receivedAt = receivedAt;
        this.processedAt = processedAt;
    }
}
