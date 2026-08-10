package com.jrdev.whatsappplatform.repository;

import com.jrdev.whatsappplatform.model.Integracion;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class IntegracionRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public IntegracionRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;

    }

    public List<Integracion> buscarTodos(){
        String sql = "SELECT * FROM integracion";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Integracion integracion = new Integracion();
            integracion.setIdIntegracion(rs.getLong("id_integracion"));
            integracion.setIdEmpresa(rs.getLong("id_empresa"));
            integracion.setNombre(rs.getString("nombre"));
            integracion.setTipo(rs.getString("tipo"));
            integracion.setSistema(rs.getString("sistema"));
            integracion.setBaseUrl(rs.getString("base_url"));
            integracion.setEstado(rs.getString("estado"));
            PGobject jsonb = rs.getObject("configuracion", PGobject.class);
            integracion.setConfiguration(convertirMetadata(jsonb));
            integracion.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
            return integracion;
        });
    }

    public Optional<Integracion> buscarById(Long id) {
        String sql = "SELECT * FROM integracion WHERE id_integracion = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
            Integracion integracion = new Integracion();
            integracion.setIdIntegracion(rs.getLong("id_integracion"));
            integracion.setIdEmpresa(rs.getLong("id_empresa"));
            integracion.setNombre(rs.getString("nombre"));
            integracion.setTipo(rs.getString("tipo"));
            integracion.setSistema(rs.getString("sistema"));
            integracion.setBaseUrl(rs.getString("base_url"));
            integracion.setEstado(rs.getString("estado"));
            PGobject jsonb = rs.getObject("configuracion", PGobject.class);
            integracion.setConfiguration(convertirMetadata(jsonb));
            integracion.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
            return integracion;
        }));
    }

    public int crear(Integracion integracion) {
        String sql = "INSERT INTO integracion (id_empresa, nombre, tipo, sistema, base_url, estado, configuracion, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, integracion.getIdEmpresa(), integracion.getNombre(), integracion.getTipo(), integracion.getSistema(), integracion.getBaseUrl(), integracion.getEstado(), convertirMetadataToJsonb(integracion.getConfiguration()), integracion.getCreatedAt());
    }

    public int actualizar(Long id, Integracion integracion) {
        String sql = "UPDATE integracion SET id_empresa = ?, nombre = ?, tipo = ?, sistema = ?, base_url = ?, estado = ?, configuracion = ?, created_at = ? WHERE id_integracion = ?";
        return jdbcTemplate.update(sql, integracion.getIdEmpresa(), integracion.getNombre(), integracion.getTipo(), integracion.getSistema(), integracion.getBaseUrl(), integracion.getEstado(), convertirMetadataToJsonb(integracion.getConfiguration()), integracion.getCreatedAt(), id);
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

    public Optional<Integracion> buscarActivaPorEmpresa(Long idEmpresa) {
        String sql = "SELECT * FROM integracion WHERE id_empresa = ? AND estado = 'ACTIVA' LIMIT 1";
        List<Integracion> resultados = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Integracion integracion = new Integracion();
            integracion.setIdIntegracion(rs.getLong("id_integracion"));
            integracion.setIdEmpresa(rs.getLong("id_empresa"));
            integracion.setNombre(rs.getString("nombre"));
            integracion.setTipo(rs.getString("tipo"));
            integracion.setSistema(rs.getString("sistema"));
            integracion.setBaseUrl(rs.getString("base_url"));
            integracion.setEstado(rs.getString("estado"));
            PGobject jsonb = rs.getObject("configuracion", PGobject.class);
            integracion.setConfiguration(convertirMetadata(jsonb));
            integracion.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
            return integracion;
        }, idEmpresa);

        return resultados.stream().findFirst();
    }
}
