package com.jrdev.whatsappplatform.dto;

import lombok.Data;

@Data
public class RegistroRequestDto {
    private String nombreCompleto;
    private String usuario;
    private String correo;
    private String password;
}