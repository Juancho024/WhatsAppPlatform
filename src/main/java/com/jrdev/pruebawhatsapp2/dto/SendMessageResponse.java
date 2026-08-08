package com.jrdev.pruebawhatsapp2.dto;

public class SendMessageResponse {

    private boolean success;
    private String message;
    private String numero;

    public SendMessageResponse() {
    }

    public SendMessageResponse(boolean success, String message, String numero) {
        this.success = success;
        this.message = message;
        this.numero = numero;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    @Override
    public String toString() {
        return "SendMessageResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", numero='" + numero + '\'' +
                '}';
    }
}