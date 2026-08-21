package com.jrdev.whatsappplatform.dto;

import lombok.Data;

@Data
public class PasswordRequestDto {
    private String nowPassword;
    private String newPassword;
    private String repeatPassword;

}
