package com.jrdev.whatsappplatform.model;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Map;


@Getter
@Setter
public class Media {
    private Long idMedia;
    private Long idMensaje;
    private String mediaType;
    private String mimeType;
    private String fileName;
    private String storageUrl;
    private String storageKey;
    private Long sizeBytes;
    private String sha256;
    private Map<String, Object> metadata;
    private OffsetDateTime createdAt;

    public Media() {
    }

    public Media(Long idMedia, Long idMensaje, String mediaType, String mimeType, String fileName, String storageUrl, String storageKey, Long sizeBytes, String sha256, Map<String, Object> metadata, OffsetDateTime createdAt) {
        this.idMedia = idMedia;
        this.idMensaje = idMensaje;
        this.mediaType = mediaType;
        this.mimeType = mimeType;
        this.fileName = fileName;
        this.storageUrl = storageUrl;
        this.storageKey = storageKey;
        this.sizeBytes = sizeBytes;
        this.sha256 = sha256;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }
}
