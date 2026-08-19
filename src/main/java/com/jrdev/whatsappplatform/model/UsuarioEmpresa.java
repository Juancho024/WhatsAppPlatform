package com.jrdev.whatsappplatform.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Data
@Getter
@Setter// Lombok te genera los getters y setters automáticamente
public class UsuarioEmpresa {
    private Long idUsuario;
    private Long idEmpresa;
    private String rolEmpresa;
    private String estado;
    private OffsetDateTime fechaVinculacion;
}