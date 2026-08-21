package com.jrdev.whatsappplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.Id;

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