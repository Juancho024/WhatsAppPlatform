package com.jrdev.whatsappplatform.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "preferencias")
@Data
public class Preferencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String language;
    private String theme;
    private boolean emailNotifications;
    private boolean pushNotifications;
    private boolean twoFactorAuth;

    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}