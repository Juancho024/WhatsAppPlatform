package com.jrdev.whatsappplatform.dto;
import lombok.Data;

@Data
public class LoginRequestDto {
    private String usuario;
    private String password;
}