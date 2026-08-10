package com.jrdev.whatsappplatform.model;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
public class Integracion {
    private Long idIntegracion;
    private Long idEmpresa;
    private String nombre;
    private String tipo;
    private String sistema;
    private String baseUrl;
    private String estado;
    private Map<String, Object> configuration;
    private OffsetDateTime createdAt;

    public Integracion() {
    }

    public Integracion(Long idIntegracion, Long idEmpresa, String nombre, String tipo, String sistema, String baseUrl, String estado, Map<String, Object> configuration, OffsetDateTime createdAt) {
        this.idIntegracion = idIntegracion;
        this.idEmpresa = idEmpresa;
        this.nombre = nombre;
        this.tipo = tipo;
        this.sistema = sistema;
        this.baseUrl = baseUrl;
        this.estado = estado;
        this.configuration = configuration;
        this.createdAt = createdAt;
    }
}
