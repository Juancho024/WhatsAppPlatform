package com.jrdev.whatsappplatform.repository;

import com.jrdev.whatsappplatform.model.Contacto;
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
public class ContactoRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public ContactoRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Contacto> buscarTodos() {
        String sql = "SELECT id_contacto, id_empresa, nombre, numero_telefono, remote_jid, remote_jid_alt, push_name, tipo, foto_url, bloqueado, metadata, fecha_creacion, fecha_actualizacion FROM contacto ORDER BY id_contacto";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Contacto contacto = new Contacto();
            contacto.setIdContacto(rs.getLong("id_contacto"));
            contacto.setIdEmpresa(rs.getLong("id_empresa"));
            contacto.setNombre(rs.getString("nombre"));
            contacto.setNumeroTelefono(rs.getString("numero_telefono"));
            contacto.setRemotejid(rs.getString("remote_jid"));
            contacto.setRemotejidalt(rs.getString("remote_jid_alt"));
            contacto.setPushName(rs.getString("push_name"));
            contacto.setTipo(rs.getString("tipo"));
            contacto.setFoto_url(rs.getString("foto_url"));
            contacto.setBloqueado(rs.getBoolean("bloqueado"));
            PGobject jsonb = rs.getObject("metadata", PGobject.class);
            contacto.setMetadata(convertirMetadata(jsonb));
            contacto.setFechaCreacion(rs.getObject("fecha_creacion", java.time.OffsetDateTime.class));
            contacto.setFechaActualizacion(rs.getObject("fecha_actualizacion", java.time.OffsetDateTime.class));
            return contacto;
        });
    }

    public Optional<Contacto> buscarPorId(Long id) {
        String sql = "SELECT id_contacto, id_empresa, nombre, numero_telefono, remote_jid, remote_jid_alt, push_name, tipo, foto_url, bloqueado, metadata, fecha_creacion, fecha_actualizacion FROM contacto WHERE id_contacto = ?";
        List<Contacto> resultados = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Contacto contacto = new Contacto();
            contacto.setIdContacto(rs.getLong("id_contacto"));
            contacto.setIdEmpresa(rs.getLong("id_empresa"));
            contacto.setNombre(rs.getString("nombre"));
            contacto.setNumeroTelefono(rs.getString("numero_telefono"));
            contacto.setRemotejid(rs.getString("remote_jid"));
            contacto.setRemotejidalt(rs.getString("remote_jid_alt"));
            contacto.setPushName(rs.getString("push_name"));
            contacto.setTipo(rs.getString("tipo"));
            contacto.setFoto_url(rs.getString("foto_url"));
            contacto.setBloqueado(rs.getBoolean("bloqueado"));
            PGobject jsonb = rs.getObject("metadata", PGobject.class);
            contacto.setMetadata(convertirMetadata(jsonb));
            contacto.setFechaCreacion(rs.getObject("fecha_creacion", OffsetDateTime.class));
            contacto.setFechaActualizacion(rs.getObject("fecha_actualizacion", OffsetDateTime.class));
            return contacto;
        }, id);
        return resultados.stream().findFirst();
    }

    public Optional<Contacto> buscarPorRemoteJid(String remoteJid) {
        String sql = "SELECT id_contacto, id_empresa, nombre, numero_telefono, remote_jid, remote_jid_alt, push_name, tipo, foto_url, bloqueado, metadata, fecha_creacion, fecha_actualizacion FROM contacto WHERE remote_jid = ?";
        List<Contacto> resultados = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Contacto contacto = new Contacto();
            contacto.setIdContacto(rs.getLong("id_contacto"));
            contacto.setIdEmpresa(rs.getLong("id_empresa"));
            contacto.setNombre(rs.getString("nombre"));
            contacto.setNumeroTelefono(rs.getString("numero_telefono"));
            contacto.setRemotejid(rs.getString("remote_jid"));
            contacto.setRemotejidalt(rs.getString("remote_jid_alt"));
            contacto.setPushName(rs.getString("push_name"));
            contacto.setTipo(rs.getString("tipo"));
            contacto.setFoto_url(rs.getString("foto_url"));
            contacto.setBloqueado(rs.getBoolean("bloqueado"));
            PGobject jsonb = rs.getObject("metadata", PGobject.class);
            contacto.setMetadata(convertirMetadata(jsonb));
            contacto.setFechaCreacion(rs.getObject("fecha_creacion", java.time.OffsetDateTime.class));
            contacto.setFechaActualizacion(rs.getObject("fecha_actualizacion", java.time.OffsetDateTime.class));
            return contacto;
        }, remoteJid);
        return resultados.stream().findFirst();
    }

    public Long crear(Contacto contacto) {
        String sql = "INSERT INTO contacto (id_empresa, nombre, numero_telefono, remote_jid, remote_jid_alt, push_name, tipo, foto_url, bloqueado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_contacto";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, contacto.getIdEmpresa());
            ps.setString(2, contacto.getNombre());
            ps.setString(3, contacto.getNumeroTelefono());
            ps.setString(4, contacto.getRemotejid());
            ps.setString(5, contacto.getRemotejidalt());
            ps.setString(6, contacto.getPushName());
            ps.setString(7, contacto.getTipo());
            ps.setString(8, contacto.getFoto_url());
            ps.setBoolean(9, contacto.isBloqueado());
            return ps;
        }, keyHolder);

        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }

    public int actualizar(Long id, Contacto contacto){
        String sql = "UPDATE contacto SET id_empresa = ?, nombre = ?, numero_telefono = ?, remote_jid = ?, remote_jid_alt = ?, push_name = ?, tipo = ?, foto_url = ?, bloqueado = ?, metadata = ? WHERE id_contacto = ?";
        return jdbcTemplate.update(sql,
                contacto.getIdEmpresa(),
                contacto.getNombre(),
                contacto.getNumeroTelefono(),
                contacto.getRemotejid(),
                contacto.getRemotejidalt(),
                contacto.getPushName(),
                contacto.getTipo(),
                contacto.getFoto_url(),
                contacto.isBloqueado(),
                convertirMetadataToJsonb(contacto.getMetadata()),
                id
        );
    }

    public int cambiarEstado(Long id, boolean bloqueado) {
        String sql = "UPDATE contacto SET bloqueado = ? WHERE id_contacto = ?";
        return jdbcTemplate.update(sql, bloqueado, id);
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