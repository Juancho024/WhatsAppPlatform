package com.jrdev.whatsappplatform.model;

import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

import java.util.List;
import java.util.Objects;

public class WhatsappInstancia {

    private Long idWhatsappInstancia;

    private Long idEmpresa;

    private String nombre;

    private String instanceName;

    private String numero;

    private String provider;

    private String apiUrl;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String apiKey;

    private String estado;

    private OffsetDateTime fechaCreacion;

    private OffsetDateTime fechaActualizacion;

    @OneToMany(mappedBy = "instancia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chat> chats;

    public WhatsappInstancia() {
    }

    public WhatsappInstancia(Long idWhatsappInstancia, Long idEmpresa, String nombre, String instanceName, String numero, String provider, String apiUrl, String apiKey, String estado, OffsetDateTime fechaCreacion, OffsetDateTime fechaActualizacion) {
        this.idWhatsappInstancia = idWhatsappInstancia;
        this.idEmpresa = idEmpresa;
        this.nombre = nombre;
        this.instanceName = instanceName;
        this.numero = numero;
        this.provider = provider;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public Long getIdWhatsappInstancia() {
        return idWhatsappInstancia;
    }

    public void setIdWhatsappInstancia(Long idWhatsappInstancia) {
        this.idWhatsappInstancia = idWhatsappInstancia;
    }

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public OffsetDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WhatsappInstancia that)) return false;
        return Objects.equals(idWhatsappInstancia, that.idWhatsappInstancia);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idWhatsappInstancia);
    }
}