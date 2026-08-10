package com.jrdev.whatsappplatform.repository;

import com.jrdev.whatsappplatform.model.Media;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class MediaRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public MediaRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Media> buscarTodo(){
        String sql = "SELECT * FROM media ORDER BY id_media";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Media media = new Media();
            media.setIdMedia(rs.getLong("id_media"));
            media.setIdMensaje(rs.getLong("id_mensaje"));
            media.setMediaType(rs.getString("media_type"));
            media.setMimeType(rs.getString("mime_type"));
            media.setFileName(rs.getString("file_name"));
            media.setStorageUrl(rs.getString("storage_url"));
            media.setStorageKey(rs.getString("storage_key"));
            media.setSizeBytes(rs.getLong("size_bytes"));
            media.setSha256(rs.getString("sha256"));
            PGobject jsonb = rs.getObject("metadata", PGobject.class);
            media.setMetadata(convertirMetadata(jsonb));
            media.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
            return media;
        });
    }

    public Optional<Media> buscarPorId(Long id){
        String sql = "SELECT * FROM media WHERE id_media = ?";
        List<Media> resultados = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Media media = new Media();
            media.setIdMedia(rs.getLong("id_media"));
            media.setIdMensaje(rs.getLong("id_mensaje"));
            media.setMediaType(rs.getString("media_type"));
            media.setMimeType(rs.getString("mime_type"));
            media.setFileName(rs.getString("file_name"));
            media.setStorageUrl(rs.getString("storage_url"));
            media.setStorageKey(rs.getString("storage_key"));
            media.setSizeBytes(rs.getLong("size_bytes"));
            media.setSha256(rs.getString("sha256"));
            PGobject jsonb = rs.getObject("metadata", PGobject.class);
            media.setMetadata(convertirMetadata(jsonb));
            media.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
            return media;
        }, id);
        return resultados.stream().findFirst();
    }

    public int crear(Media media){
        String sql = "INSERT INTO media (id_mensaje, media_type, mime_type, file_name, storage_url, storage_key, size_bytes, sha256, metadata, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                media.getIdMensaje(),
                media.getMediaType(),
                media.getMimeType(),
                media.getFileName(),
                media.getStorageUrl(),
                media.getStorageKey(),
                media.getSizeBytes(),
                media.getSha256(),
                convertirMetadataToJsonb(media.getMetadata()),
                media.getCreatedAt()
        );
    }

    public int actualizar(Long id, Media media){
        String sql = "UPDATE media SET id_mensaje = ?, media_type = ?, mime_type = ?, file_name = ?, storage_url = ?, storage_key = ?, size_bytes = ?, sha256 = ?, metadata = ?, created_at = ? WHERE id_media = ?";
        return jdbcTemplate.update(sql,
                media.getIdMensaje(),
                media.getMediaType(),
                media.getMimeType(),
                media.getFileName(),
                media.getStorageUrl(),
                media.getStorageKey(),
                media.getSizeBytes(),
                media.getSha256(),
                convertirMetadataToJsonb(media.getMetadata()),
                media.getCreatedAt(),
                id
        );
    }

    private String convertirMetadataToString(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            throw new RuntimeException("Error convirtiendo metadata a JSON", e);
        }
    }

    private Map<String, Object> convertirMetadata(PGobject jsonb) {
        if (jsonb == null || jsonb.getValue() == null) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(jsonb.getValue(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error convirtiendo metadata JSONB", e);
        }
    }

    private PGobject convertirMetadataToJsonb(Map<String, Object> metadata) {
        try {
            PGobject jsonb = new PGobject();
            jsonb.setType("jsonb");
            jsonb.setValue(convertirMetadataToString(metadata));
            return jsonb;
        } catch (Exception e) {
            throw new RuntimeException("Error convirtiendo metadata a JSONB", e);
        }
    }
}
