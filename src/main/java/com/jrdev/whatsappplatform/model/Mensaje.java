package com.jrdev.whatsappplatform.model;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
public class Mensaje {
    private Long idMensaje;
    private Long idChat;
    private String evolutionMessageId;
    private String contenido;
    private String tipo;
    private String senderJid;
    private boolean enviadoPorNosotros;
    private String direccion;
    private String estado;
    private Long replyToMessageId;
    private Map<String, Object> rawPlayload;
    private OffsetDateTime fechaMensaje;

    public Mensaje() {
    }

    public Mensaje(Long idMensaje, Long idChat, String evolutionMessageId, String contenido, String tipo, String senderJid, boolean enviadoPorNosotros, String direccion, String estado, Long replyToMessageId, Map<String, Object> rawPlayload, OffsetDateTime fechaMensaje) {
        this.idMensaje = idMensaje;
        this.idChat = idChat;
        this.evolutionMessageId = evolutionMessageId;
        this.contenido = contenido;
        this.tipo = tipo;
        this.senderJid = senderJid;
        this.enviadoPorNosotros = enviadoPorNosotros;
        this.direccion = direccion;
        this.estado = estado;
        this.replyToMessageId = replyToMessageId;
        this.rawPlayload = rawPlayload;
        this.fechaMensaje = fechaMensaje;
    }

}
