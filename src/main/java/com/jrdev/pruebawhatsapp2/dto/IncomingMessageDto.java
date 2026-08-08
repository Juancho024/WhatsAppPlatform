package com.jrdev.pruebawhatsapp2.dto;

import java.time.LocalDateTime;

public class IncomingMessageDto {

    private String messageId;
    private String numero;
    private String nombre;
    private String mensaje;
    private LocalDateTime fecha;
    private boolean grupo;

    public IncomingMessageDto() {
    }

    public IncomingMessageDto(String messageId,
                              String numero,
                              String nombre,
                              String mensaje,
                              LocalDateTime fecha,
                              boolean grupo) {
        this.messageId = messageId;
        this.numero = numero;
        this.nombre = nombre;
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.grupo = grupo;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public boolean isGrupo() {
        return grupo;
    }

    public void setGrupo(boolean grupo) {
        this.grupo = grupo;
    }

    @Override
    public String toString() {
        return "IncomingMessageDto{" +
                "messageId='" + messageId + '\'' +
                ", numero='" + numero + '\'' +
                ", nombre='" + nombre + '\'' +
                ", mensaje='" + mensaje + '\'' +
                ", fecha=" + fecha +
                ", grupo=" + grupo +
                '}';
    }
}