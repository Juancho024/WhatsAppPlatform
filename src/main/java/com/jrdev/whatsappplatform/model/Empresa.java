package com.jrdev.whatsappplatform.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public class Empresa {

    private Long idEmpresa;
    private String nombre;
    private String identificacion;
    private String email;
    private String telefono;
    private String estado;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaActualizacion;

    public Empresa() {
    }

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WhatsappInstancia> instancias;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contacto> contactos;

    public Empresa(Long idEmpresa, String nombre, String identificacion, String email, String telefono, String estado, OffsetDateTime fechaCreacion, OffsetDateTime fechaActualizacion) {
        this.idEmpresa = idEmpresa;
        this.nombre = nombre;
        this.identificacion = identificacion;
        this.email = email;
        this.telefono = telefono;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
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

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
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
        if (!(o instanceof Empresa empresa)) return false;
        return Objects.equals(idEmpresa, empresa.idEmpresa);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idEmpresa);
    }
}