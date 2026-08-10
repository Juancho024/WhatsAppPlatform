package com.jrdev.whatsappplatform.repository;

import com.jrdev.whatsappplatform.model.Evento;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class EventoRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public EventoRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Evento> buscarTodos(){
        String sql = "SELECT * FROM evento";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Evento evento = new Evento();
            evento.setIdEvento(rs.getLong("id_evento"));
            evento.setIdWhatsAppInstancia(rs.getLong("id_whatsapp_instancia"));
            evento.setTipoEvento(rs.getString("tipo_evento"));
            evento.setExternal_event_id(rs.getString("external_event_id"));
            PGobject jsonb = rs.getObject("payload", PGobject.class);
            evento.setPayload(convertirMetadata(jsonb));
            evento.setEstado(rs.getString("estado"));
            evento.setError(rs.getString("error_message"));
            evento.setReceivedAt(rs.getObject("received_at", java.time.OffsetDateTime.class));
            evento.setProcessedAt(rs.getObject("processed_at", java.time.OffsetDateTime.class));
            return evento;
        });
    }

    public Optional<Evento> buscarPorId(Long idEvento) {
        String sql = "SELECT * FROM evento WHERE id_evento = ?";
        List<Evento> eventos = jdbcTemplate.query(sql, new Object[]{idEvento}, (rs, rowNum) -> {
            Evento evento = new Evento();
            evento.setIdEvento(rs.getLong("id_evento"));
            evento.setIdWhatsAppInstancia(rs.getLong("id_whatsapp_instancia"));
            evento.setTipoEvento(rs.getString("tipo_evento"));
            evento.setExternal_event_id(rs.getString("external_event_id"));
            PGobject jsonb = rs.getObject("payload", PGobject.class);
            evento.setPayload(convertirMetadata(jsonb));
            evento.setEstado(rs.getString("estado"));
            evento.setError(rs.getString("error_message"));
            evento.setReceivedAt(rs.getObject("received_at", java.time.OffsetDateTime.class));
            evento.setProcessedAt(rs.getObject("processed_at", java.time.OffsetDateTime.class));
            return evento;
        });
        return eventos.stream().findFirst();
    }

    public int crear(Evento evento){
        String sql = "INSERT INTO evento (id_whatsapp_instancia, tipo_evento, external_event_id, payload, estado, error_message, received_at, processed_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, evento.getIdWhatsAppInstancia(), evento.getTipoEvento(), evento.getExternal_event_id(), convertirMetadataToJsonb(evento.getPayload()), evento.getEstado(), evento.getError(), evento.getReceivedAt(), evento.getProcessedAt());
    }

    public int actualizar(Long id, Evento evento){
        String sql = "UPDATE evento SET id_whatsapp_instancia = ?, tipo_evento = ?, external_event_id = ?, payload = ?, estado = ?, error_message = ?, received_at = ?, processed_at = ? WHERE id_evento = ?";
        return jdbcTemplate.update(sql, evento.getIdWhatsAppInstancia(), evento.getTipoEvento(), evento.getExternal_event_id(), convertirMetadataToJsonb(evento.getPayload()), evento.getEstado(), evento.getError(), evento.getReceivedAt(), evento.getProcessedAt(), id);
    }

    public int marcarProcesado(Long id) {
        String sql = """
                UPDATE evento
                SET estado = 'PROCESADO',
                    processed_at = CURRENT_TIMESTAMP,
                    error_message = NULL
                WHERE id_evento = ?
                """;
        return jdbcTemplate.update(sql, id);
    }

    public int marcarError(Long id, String error) {
        String sql = """
                UPDATE evento
                SET estado = 'ERROR',
                    error_message = ?,
                    processed_at = CURRENT_TIMESTAMP
                WHERE id_evento = ?
                """;
        return jdbcTemplate.update(sql, error, id);
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
