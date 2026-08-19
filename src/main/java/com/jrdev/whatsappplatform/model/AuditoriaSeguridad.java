package com.jrdev.whatsappplatform.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Data
@Getter
@Setter
public class AuditoriaSeguridad {
    private Long idAuditoria;
    private Long idEmpresa;
    private Long idUsuario;
    private String accion;
    private String tablaAfectada;
    private Long registroId;
    private String detalles; // Lo manejamos como String JSON
    private OffsetDateTime fechaAccion;
}