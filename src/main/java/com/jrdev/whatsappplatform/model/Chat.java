package com.jrdev.whatsappplatform.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Chat {
    private Long idChat;
    private Long idWhatsAppInstancia;
    private Long idContacto;
    private String remotejid;
    private String titulo;
    private String estado;
    private int unread_count;
    private OffsetDateTime last_message_at;
    private OffsetDateTime ultima_actividad;
    private Map<String, Object> metadata;
    private OffsetDateTime fechaCreacion;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mensaje> mensajes;

    public Chat() {}

    public Chat(Long idChat, Long idWhatsAppInstancia, Long idContacto, String remotejid, String titulo, String estado, int unread_count, OffsetDateTime last_message_at, OffsetDateTime ultima_actividad, Map<String, Object> metadata, OffsetDateTime fechaCreacion) {
        this.idChat = idChat;
        this.idWhatsAppInstancia = idWhatsAppInstancia;
        this.idContacto = idContacto;
        this.remotejid = remotejid;
        this.titulo = titulo;
        this.estado = estado;
        this.unread_count = unread_count;
        this.last_message_at = last_message_at;
        this.ultima_actividad = ultima_actividad;
        this.metadata = metadata;
        this.fechaCreacion = fechaCreacion;
    }
}
