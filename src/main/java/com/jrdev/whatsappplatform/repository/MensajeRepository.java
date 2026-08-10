package com.jrdev.whatsappplatform.repository;

import com.jrdev.whatsappplatform.model.Mensaje;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class MensajeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public MensajeRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Mensaje> buscarTodos() {
        String sql = "SELECT id_mensaje, id_chat, evolution_message_id, contenido, tipo, sender_jid, enviado_por_nosotros, direction, estado, reply_to_message_id, raw_payload, fecha_mensaje FROM mensaje ORDER BY id_mensaje";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Mensaje mensaje = new Mensaje();
            mensaje.setIdMensaje(rs.getLong("id_mensaje"));
            mensaje.setIdChat(rs.getLong("id_chat"));
            mensaje.setEvolutionMessageId(rs.getString("evolution_message_id"));
            mensaje.setContenido(rs.getString("contenido"));
            mensaje.setTipo(rs.getString("tipo"));
            mensaje.setSenderJid(rs.getString("sender_jid"));
            mensaje.setEnviadoPorNosotros(rs.getBoolean("enviado_por_nosotros"));
            mensaje.setDireccion(rs.getString("direction"));
            mensaje.setEstado(rs.getString("estado"));
            mensaje.setReplyToMessageId(rs.getLong("reply_to_message_id"));
            PGobject jsonb = rs.getObject("raw_payload", PGobject.class);
            mensaje.setRawPlayload(convertirMetadata(jsonb));
            mensaje.setFechaMensaje(rs.getObject("fecha_mensaje", java.time.OffsetDateTime.class));
            return mensaje;
        });
    }

    public Optional<Mensaje> buscarPorId(Long id) {
        String sql = "SELECT id_mensaje, id_chat, evolution_message_id, contenido, tipo, sender_jid, enviado_por_nosotros, direction, estado, reply_to_message_id, raw_payload, fecha_mensaje FROM mensaje WHERE id_mensaje = ?";
        List<Mensaje> resultados = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Mensaje mensaje = new Mensaje();
            mensaje.setIdMensaje(rs.getLong("id_mensaje"));
            mensaje.setIdChat(rs.getLong("id_chat"));
            mensaje.setEvolutionMessageId(rs.getString("evolution_message_id"));
            mensaje.setContenido(rs.getString("contenido"));
            mensaje.setTipo(rs.getString("tipo"));
            mensaje.setSenderJid(rs.getString("sender_jid"));
            mensaje.setEnviadoPorNosotros(rs.getBoolean("enviado_por_nosotros"));
            mensaje.setDireccion(rs.getString("direction"));
            mensaje.setEstado(rs.getString("estado"));
            mensaje.setReplyToMessageId(rs.getLong("reply_to_message_id"));
            PGobject jsonb = rs.getObject("raw_payload", PGobject.class);
            mensaje.setRawPlayload(convertirMetadata(jsonb));
            mensaje.setFechaMensaje(rs.getObject("fecha_mensaje", java.time.OffsetDateTime.class));
            return mensaje;
        }, id);
        return resultados.stream().findFirst();
    }

    public Long crear(Mensaje mensaje) {
        String sql = "INSERT INTO mensaje (id_chat, evolution_message_id, contenido, tipo, sender_jid, enviado_por_nosotros, direction, estado, reply_to_message_id, raw_payload, fecha_mensaje) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_mensaje";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, mensaje.getIdChat());
            ps.setString(2, mensaje.getEvolutionMessageId());
            ps.setString(3, mensaje.getContenido());
            ps.setString(4, mensaje.getTipo());
            ps.setString(5, mensaje.getSenderJid());
            ps.setBoolean(6, mensaje.isEnviadoPorNosotros());
            ps.setString(7, mensaje.getDireccion());
            ps.setString(8, mensaje.getEstado());
            ps.setObject(9, mensaje.getReplyToMessageId()); // Puede ser null
            ps.setObject(10, convertirMetadataToJsonb(mensaje.getRawPlayload())); // Usa tu método
            ps.setObject(11, mensaje.getFechaMensaje());
            return ps;
        }, keyHolder);

        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }

    public int actualizar(Long id, Mensaje mensaje) {
        String sql = "UPDATE mensaje SET id_chat = ?, evolution_message_id = ?, contenido = ?, tipo = ?, sender_jid = ?, enviado_por_nosotros = ?, direction = ?, estado = ?, reply_to_message_id = ?, raw_payload = ?, fecha_mensaje = ? WHERE id_mensaje = ?";
        return jdbcTemplate.update(sql, mensaje.getIdChat(), mensaje.getEvolutionMessageId(), mensaje.getContenido(), mensaje.getTipo(), mensaje.getSenderJid(), mensaje.isEnviadoPorNosotros(), mensaje.getDireccion(), mensaje.getEstado(), mensaje.getReplyToMessageId(), convertirMetadataToJsonb(mensaje.getRawPlayload()), mensaje.getFechaMensaje(), id);
    }

    public List<Mensaje> buscarPorChat(Long idChat) {
        String sql = """
                SELECT
                    id_mensaje, id_chat,
                    evolution_message_id,
                    contenido, tipo,
                    sender_jid,
                    enviado_por_nosotros,
                    direction,
                    estado,
                    reply_to_message_id,
                    raw_payload,
                    fecha_mensaje
                FROM mensaje
                WHERE id_chat = ?
                ORDER BY fecha_mensaje ASC
                """;
        return jdbcTemplate.query(sql, this::mapearMensaje, idChat);
    }

    private Mensaje mapearMensaje(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Mensaje mensaje = new Mensaje();
        mensaje.setIdMensaje(rs.getLong("id_mensaje"));
        mensaje.setIdChat(rs.getLong("id_chat"));
        mensaje.setEvolutionMessageId(rs.getString("evolution_message_id"));
        mensaje.setContenido(rs.getString("contenido"));
        mensaje.setTipo(rs.getString("tipo"));
        mensaje.setSenderJid(rs.getString("sender_jid"));
        mensaje.setEnviadoPorNosotros(rs.getBoolean("enviado_por_nosotros"));
        mensaje.setDireccion(rs.getString("direction"));
        mensaje.setEstado(rs.getString("estado"));
        Long replyId = rs.getObject("reply_to_message_id", Long.class);
        mensaje.setReplyToMessageId(replyId);
        PGobject jsonb = rs.getObject("raw_payload", PGobject.class);
        mensaje.setRawPlayload(convertirMetadata(jsonb));
        mensaje.setFechaMensaje(rs.getObject("fecha_mensaje", OffsetDateTime.class));
        return mensaje;
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
            return objectMapper.readValue(jsonb.getValue(), new TypeReference<Map<String, Object>>() {
            });
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
