package com.jrdev.pruebawhatsapp2.dto;

public class ContactDto {

    private Long id;
    private String nombre;
    private String numero;
    private String fotoPerfil;
    private boolean bloqueado;

    public ContactDto() {
    }

    public ContactDto(Long id, String nombre, String numero, String fotoPerfil, boolean bloqueado) {
        this.id = id;
        this.nombre = nombre;
        this.numero = numero;
        this.fotoPerfil = fotoPerfil;
        this.bloqueado = bloqueado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    @Override
    public String toString() {
        return "ContactDto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", numero='" + numero + '\'' +
                ", fotoPerfil='" + fotoPerfil + '\'' +
                ", bloqueado=" + bloqueado +
                '}';
    }
}