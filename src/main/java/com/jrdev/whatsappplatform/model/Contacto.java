package com.jrdev.whatsappplatform.model;

import java.time.OffsetDateTime;
import java.util.Map;

public class Contacto {
    private Long idContacto;
    private Long idEmpresa;
    private String nombre;
    private String numeroTelefono;
    private String remotejid;
    private String remotejidalt;
    private String pushName;
    private String tipo;
    private String foto_url;
    private boolean bloqueado;
    private Map<String, Object> metadata;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaActualizacion;

    public Contacto() {
    }

    public Contacto(Long idContacto, Long idEmpresa, String nombre, String numeroTelefono, String remotejid, String remotejidalt, String pushName, String tipo, String foto_url, boolean bloqueado, Map<String, Object> metadata, OffsetDateTime fechaCreacion, OffsetDateTime fechaActualizacion) {
        this.idContacto = idContacto;
        this.idEmpresa = idEmpresa;
        this.nombre = nombre;
        this.numeroTelefono = numeroTelefono;
        this.remotejid = remotejid;
        this.remotejidalt = remotejidalt;
        this.pushName = pushName;
        this.tipo = tipo;
        this.foto_url = foto_url;
        this.bloqueado = bloqueado;
        this.metadata = metadata;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getIdContacto() {
        return idContacto;
    }

    public void setIdContacto(Long idContacto) {
        this.idContacto = idContacto;
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

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public String getRemotejid() {
        return remotejid;
    }

    public void setRemotejid(String remotejid) {
        this.remotejid = remotejid;
    }

    public String getRemotejidalt() {
        return remotejidalt;
    }

    public void setRemotejidalt(String remotejidalt) {
        this.remotejidalt = remotejidalt;
    }

    public String getPushName() {
        return pushName;
    }

    public void setPushName(String pushName) {
        this.pushName = pushName;
    }

    public String getFoto_url() {
        return foto_url;
    }

    public void setFoto_url(String foto_url) {
        this.foto_url = foto_url;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
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
}
