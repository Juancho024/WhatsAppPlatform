package com.jrdev.whatsappplatform.repository;

import com.jrdev.whatsappplatform.model.Chat;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ChatRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public ChatRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;

    }

    public List<Chat> buscarTodos(){
        String sql = "SELECT id_chat, id_whatsapp_instancia, id_contacto, remote_jid, titulo, estado, unread_count, last_message_at, ultima_actividad, metadata, fecha_creacion FROM chat ORDER BY id_chat";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Chat chat = new Chat();
            chat.setIdChat(rs.getLong("id_chat"));
            chat.setIdWhatsAppInstancia(rs.getLong("id_whatsapp_instancia"));
            chat.setIdContacto(rs.getLong("id_contacto"));
            chat.setRemotejid(rs.getString("remote_jid"));
            chat.setTitulo(rs.getString("titulo"));
            chat.setEstado(rs.getString("estado"));
            chat.setUnread_count(rs.getInt("unread_count"));
            chat.setLast_message_at(rs.getObject("last_message_at", java.time.OffsetDateTime.class));
            chat.setUltima_actividad(rs.getObject("ultima_actividad", java.time.OffsetDateTime.class));
            PGobject jsonb = rs.getObject("metadata", PGobject.class);
            chat.setMetadata(convertirMetadata(jsonb));
            return chat;
        });
    }

    public Optional<Chat> buscarPorId(Long idChat) {
        String sql = "SELECT id_chat, id_whatsapp_instancia, id_contacto, remote_jid, titulo, estado, unread_count, last_message_at, ultima_actividad, metadata, fecha_creacion FROM chat WHERE id_chat = ?";
        List<Chat> resultados = jdbcTemplate.query(sql, (rs, rowNum) -> {
                Chat chat = new Chat();
                chat.setIdChat(rs.getLong("id_chat"));
                chat.setIdWhatsAppInstancia(rs.getLong("id_whatsapp_instancia"));
                chat.setIdContacto(rs.getLong("id_contacto"));
                chat.setRemotejid(rs.getString("remote_jid"));
                chat.setTitulo(rs.getString("titulo"));
                chat.setEstado(rs.getString("estado"));
                chat.setUnread_count(rs.getInt("unread_count"));
                chat.setLast_message_at(rs.getObject("last_message_at", java.time.OffsetDateTime.class));
                chat.setUltima_actividad(rs.getObject("ultima_actividad", java.time.OffsetDateTime.class));
                PGobject jsonb = rs.getObject("metadata", PGobject.class);
                chat.setMetadata(convertirMetadata(jsonb));
                return chat;
        }, idChat);
        return resultados.stream().findFirst();
    }

//    public int crear(Chat chat){
//        String sql = "INSERT INTO chat (id_whatsapp_instancia, id_contacto, remote_jid, titulo, estado, unread_count, last_message_at, ultima_actividad, metadata) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//        return jdbcTemplate.update(sql,
//                chat.getIdWhatsAppInstancia(),
//                chat.getIdContacto(),
//                chat.getRemotejid(),
//                chat.getTitulo(),
//                chat.getEstado(),
//                chat.getUnread_count(),
//                chat.getLast_message_at(),
//                chat.getUltima_actividad(),
//                convertirMetadataToJsonb(chat.getMetadata())
//                );
//    }

    public Long crear(Chat chat) {
        String sql = "INSERT INTO chat (id_whatsapp_instancia, id_contacto, remote_jid, titulo, estado, unread_count, last_message_at, ultima_actividad, metadata) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_chat";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, chat.getIdWhatsAppInstancia());
            ps.setLong(2, chat.getIdContacto());
            ps.setString(3, chat.getRemotejid());
            ps.setString(4, chat.getTitulo());
            ps.setString(5, chat.getEstado());
            ps.setInt(6, chat.getUnread_count());
            ps.setObject(7, chat.getLast_message_at());
            ps.setObject(8, chat.getUltima_actividad());
            ps.setObject(9, convertirMetadataToJsonb(chat.getMetadata()));
            return ps;
        }, keyHolder);

        // Retornamos el ID autogenerado
        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }

    public Optional<Chat> buscarPorInstanciaYContacto(Long idInstancia, Long idContacto) {
        String sql = "SELECT id_chat, id_whatsapp_instancia, id_contacto, remote_jid, titulo, estado, unread_count, last_message_at, ultima_actividad, metadata, fecha_creacion FROM chat WHERE id_whatsapp_instancia = ? AND id_contacto = ?";
        List<Chat> resultados = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Chat chat = new Chat();
            chat.setIdChat(rs.getLong("id_chat"));
            chat.setIdWhatsAppInstancia(rs.getLong("id_whatsapp_instancia"));
            chat.setIdContacto(rs.getLong("id_contacto"));
            chat.setRemotejid(rs.getString("remote_jid"));
            chat.setTitulo(rs.getString("titulo"));
            chat.setEstado(rs.getString("estado"));
            chat.setUnread_count(rs.getInt("unread_count"));
            chat.setLast_message_at(rs.getObject("last_message_at", java.time.OffsetDateTime.class));
            chat.setUltima_actividad(rs.getObject("ultima_actividad", java.time.OffsetDateTime.class));
            PGobject jsonb = rs.getObject("metadata", PGobject.class);
            chat.setMetadata(convertirMetadata(jsonb));
            return chat;
        }, idInstancia, idContacto);
        return resultados.stream().findFirst();
    }

    public int actualizar(Long id, Chat chat){
        String sql = "UPDATE chat SET id_whatsapp_instancia = ?, id_contacto = ?, remote_jid = ?, titulo = ?, estado = ?, unread_count = ?, last_message_at = ?, ultima_actividad = ?, metadata = ? WHERE id_chat = ?";
        return jdbcTemplate.update(sql,
                chat.getIdWhatsAppInstancia(),
                chat.getIdContacto(),
                chat.getRemotejid(),
                chat.getTitulo(),
                chat.getEstado(),
                chat.getUnread_count(),
                chat.getLast_message_at(),
                chat.getUltima_actividad(),
                convertirMetadataToJsonb(chat.getMetadata()), id);
    }

    public int cambiarEstado(Long id, String estado){
        String sql = "UPDATE chat SET estado = ? WHERE id_chat = ?";
        return jdbcTemplate.update(sql, estado, id);
    }

    private String convertirMetadataToString(
            Map<String, Object> metadata
    ) {

        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }

        try {

            return objectMapper.writeValueAsString(metadata);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error convirtiendo metadata a JSON",
                    e
            );
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

    private PGobject convertirMetadataToJsonb(
            Map<String, Object> metadata
    ) {

        try {

            PGobject jsonb = new PGobject();

            jsonb.setType("jsonb");

            jsonb.setValue(
                    convertirMetadataToString(metadata)
            );

            return jsonb;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error convirtiendo metadata a JSONB",
                    e
            );
        }
    }

    public int actualizarUltimaActividad(Long idChat) {

        String sql = """
            UPDATE chat
            SET
                ultima_actividad = CURRENT_TIMESTAMP,
                last_message_at = CURRENT_TIMESTAMP
            WHERE id_chat = ?
            """;

        return jdbcTemplate.update(sql, idChat);
    }
    public int incrementarUnreadCount(Long idChat) {

        String sql = """
            UPDATE chat
            SET unread_count = unread_count + 1
            WHERE id_chat = ?
            """;

        return jdbcTemplate.update(sql, idChat);
    }

    public List<Chat> buscarPorEmpresa(Long idEmpresa) {
        String sql = """
            SELECT c.id_chat, c.id_whatsapp_instancia, c.id_contacto, c.remote_jid, 
                   c.titulo, c.estado, c.unread_count, c.last_message_at, 
                   c.ultima_actividad, c.metadata, c.fecha_creacion
            FROM chat c
            JOIN whatsapp_instancia wi ON c.id_whatsapp_instancia = wi.id_whatsapp_instancia
            WHERE wi.id_empresa = ?
            ORDER BY c.ultima_actividad DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Chat chat = new Chat();
            chat.setIdChat(rs.getLong("id_chat"));
            chat.setIdWhatsAppInstancia(rs.getLong("id_whatsapp_instancia"));
            chat.setIdContacto(rs.getLong("id_contacto"));
            chat.setRemotejid(rs.getString("remote_jid"));
            chat.setTitulo(rs.getString("titulo"));
            chat.setEstado(rs.getString("estado"));
            chat.setUnread_count(rs.getInt("unread_count"));
            chat.setLast_message_at(rs.getObject("last_message_at", java.time.OffsetDateTime.class));
            chat.setUltima_actividad(rs.getObject("ultima_actividad", java.time.OffsetDateTime.class));
            PGobject jsonb = rs.getObject("metadata", PGobject.class);
            chat.setMetadata(convertirMetadata(jsonb));
            return chat;
        }, idEmpresa);
    }

    public List<Chat> buscarPorInstancia(Long idWhatsappInstancia) {
        String sql = """
            SELECT id_chat, id_whatsapp_instancia, id_contacto, remote_jid, 
                   titulo, estado, unread_count, last_message_at, 
                   ultima_actividad, metadata, fecha_creacion
            FROM chat
            WHERE id_whatsapp_instancia = ?
            ORDER BY ultima_actividad DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Chat chat = new Chat();
            chat.setIdChat(rs.getLong("id_chat"));
            chat.setIdWhatsAppInstancia(rs.getLong("id_whatsapp_instancia"));
            chat.setIdContacto(rs.getLong("id_contacto"));
            chat.setRemotejid(rs.getString("remote_jid"));
            chat.setTitulo(rs.getString("titulo"));
            chat.setEstado(rs.getString("estado"));
            chat.setUnread_count(rs.getInt("unread_count"));
            chat.setLast_message_at(rs.getObject("last_message_at", java.time.OffsetDateTime.class));
            chat.setUltima_actividad(rs.getObject("ultima_actividad", java.time.OffsetDateTime.class));
            PGobject jsonb = rs.getObject("metadata", PGobject.class);
            chat.setMetadata(convertirMetadata(jsonb));
            return chat;
        }, idWhatsappInstancia);
    }
}
