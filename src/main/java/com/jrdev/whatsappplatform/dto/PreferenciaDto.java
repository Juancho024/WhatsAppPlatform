package com.jrdev.whatsappplatform.dto;

import lombok.Data;

@Data
public class PreferenciaDto {
    private String language;
    private String theme;
    private boolean emailNotifications;
    private boolean pushNotifications;
    private boolean twoFactorAuth;
}
