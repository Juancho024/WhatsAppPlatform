package com.jrdev.pruebawhatsapp2.dto;

public class SendMessageRequest {

    private String numero;
    private String mensaje;

    public SendMessageRequest() {
    }

    public SendMessageRequest(String numero, String mensaje) {
        this.numero = numero;
        this.mensaje = mensaje;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    @Override
    public String toString() {
        return "SendMessageRequest{" +
                "numero='" + numero + '\'' +
                ", mensaje='" + mensaje + '\'' +
                '}';
    }
}