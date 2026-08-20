package com.jrdev.whatsappplatform.repository;

import com.jrdev.whatsappplatform.model.WhatsappInstancia;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WhatsappInstanciaRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<WhatsappInstancia> buscarTodas() {

        String sql = "SELECT id_whatsapp_instancia, id_empresa, nombre, instance_name, numero, provider, api_url, api_key, estado, fecha_creacion, fecha_actualizacion FROM whatsapp_instancia ORDER BY id_whatsapp_instancia ";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            WhatsappInstancia instancia = new WhatsappInstancia();
            instancia.setIdWhatsappInstancia(rs.getLong("id_whatsapp_instancia"));
            instancia.setIdEmpresa(rs.getLong("id_empresa"));
            instancia.setNombre(rs.getString("nombre"));
            instancia.setInstanceName(rs.getString("instance_name"));
            instancia.setNumero(rs.getString("numero"));
            instancia.setProvider(rs.getString("provider"));
            instancia.setApiUrl(rs.getString("api_url"));
            instancia.setApiKey(rs.getString("api_key"));
            instancia.setEstado(rs.getString("estado"));
            instancia.setFechaCreacion(rs.getObject("fecha_creacion", java.time.OffsetDateTime.class));
            instancia.setFechaActualizacion(rs.getObject("fecha_actualizacion", java.time.OffsetDateTime.class));

            return instancia;
        });
    }

    public Optional<WhatsappInstancia> buscarPorId(Long id) {
        String sql = "SELECT id_whatsapp_instancia, id_empresa, nombre, instance_name, numero, provider, api_url, api_key, estado, fecha_creacion, fecha_actualizacion FROM whatsapp_instancia WHERE id_whatsapp_instancia = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
            WhatsappInstancia instancia = new WhatsappInstancia();
            instancia.setIdWhatsappInstancia(rs.getLong("id_whatsapp_instancia"));
            instancia.setIdEmpresa(rs.getLong("id_empresa"));
            instancia.setNombre(rs.getString("nombre"));
            instancia.setInstanceName(rs.getString("instance_name"));
            instancia.setNumero(rs.getString("numero"));
            instancia.setProvider(rs.getString("provider"));
            instancia.setApiUrl(rs.getString("api_url"));
            instancia.setApiKey(rs.getString("api_key"));
            instancia.setEstado(rs.getString("estado"));
            instancia.setFechaCreacion(rs.getObject("fecha_creacion", java.time.OffsetDateTime.class));
            instancia.setFechaActualizacion(rs.getObject("fecha_actualizacion", java.time.OffsetDateTime.class));

            return instancia;
        }));
    }

    public Optional<WhatsappInstancia> buscarPorInstanceName(String instanceName) {
        String sql = "SELECT id_whatsapp_instancia, id_empresa, nombre, instance_name, numero, provider, api_url, api_key, estado, fecha_creacion, fecha_actualizacion FROM whatsapp_instancia WHERE instance_name = ?";
        List<WhatsappInstancia> resultados = jdbcTemplate.query(sql, (rs, rowNum) -> {
            WhatsappInstancia instancia = new WhatsappInstancia();
            instancia.setIdWhatsappInstancia(rs.getLong("id_whatsapp_instancia"));
            instancia.setIdEmpresa(rs.getLong("id_empresa"));
            instancia.setNombre(rs.getString("nombre"));
            instancia.setInstanceName(rs.getString("instance_name"));
            instancia.setNumero(rs.getString("numero"));
            instancia.setProvider(rs.getString("provider"));
            instancia.setApiUrl(rs.getString("api_url"));
            instancia.setApiKey(rs.getString("api_key"));
            instancia.setEstado(rs.getString("estado"));
            instancia.setFechaCreacion(rs.getObject("fecha_creacion", java.time.OffsetDateTime.class));
            instancia.setFechaActualizacion(rs.getObject("fecha_actualizacion", java.time.OffsetDateTime.class));
            return instancia;
        }, instanceName);
        return resultados.stream().findFirst();
    }

    public Long crear(WhatsappInstancia instancia) {

        String sql = """
                INSERT INTO whatsapp_instancia (
                    id_empresa,
                    nombre,
                    instance_name,
                    numero,
                    provider,
                    api_url,
                    api_key,
                    estado
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id_whatsapp_instancia
                """;

        return jdbcTemplate.queryForObject(sql, Long.class, instancia.getIdEmpresa(), instancia.getNombre(), instancia.getInstanceName(), instancia.getNumero(), instancia.getProvider(), instancia.getApiUrl(), instancia.getApiKey(), instancia.getEstado());
    }

//    public Long crear(WhatsappInstancia instancia) {
//        String sql = """
//                INSERT INTO whatsapp_instancia (
//                    id_empresa,
//                    nombre,
//                    instance_name,
//                    numero,
//                    provider,
//                    api_url,
//                    api_key,
//                    estado
//                )
//                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
//                """;
//        KeyHolder keyHolder = new GeneratedKeyHolder();
//        jdbcTemplate.update(connection -> {
//            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
//            ps.setLong(1, instancia.getIdEmpresa());
//            ps.setString(2, instancia.getNombre());
//            ps.setString(3, instancia.getInstanceName());
//            ps.setString(4, instancia.getNumero());
//            ps.setString(5, instancia.getProvider());
//            ps.setString(6, instancia.getApiUrl());
//            ps.setString(7, instancia.getApiKey());
//            ps.setString(8, instancia.getEstado());
//            return ps;
//        }, keyHolder);
//        Number key = keyHolder.getKey();
//        if (key == null) {
//            throw new RuntimeException("No se pudo obtener el ID de la instancia");
//        }
//        return key.longValue();
//    }

    public boolean existePorInstanceName(String instanceName) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM whatsapp_instancia
                    WHERE instance_name = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, instanceName));
    }

    public int actualizarEstadoPorInstanceName(String instanceName, String nuevoEstado) {
        String sql = "UPDATE whatsapp_instancia SET estado = ?, fecha_actualizacion = CURRENT_TIMESTAMP WHERE instance_name = ?";
        return jdbcTemplate.update(sql, nuevoEstado, instanceName);
    }

    public int actualizar(Long id, WhatsappInstancia instancia) {
        String sql = "UPDATE whatsapp_instancia SET id_empresa = ?, nombre = ?, instance_name = ?, numero = ?, provider = ?, api_url = ?, api_key = ?, estado = ? WHERE id_whatsapp_instancia = ?";
        return jdbcTemplate.update(sql, instancia.getIdEmpresa(), instancia.getNombre(), instancia.getInstanceName(), instancia.getNumero(), instancia.getProvider(), instancia.getApiUrl(), instancia.getApiKey(), instancia.getEstado(), id);
    }

    public int cambiarEstado(Long id, String estado) {
        String sql = "UPDATE whatsapp_instancia SET estado = ?, fecha_actualizacion = CURRENT_TIMESTAMP WHERE id_whatsapp_instancia = ?";
        return jdbcTemplate.update(sql, estado, id);
    }

    public List<WhatsappInstancia> buscarPorEmpresa(Long idEmpresa) {
        String sql = "SELECT id_whatsapp_instancia, id_empresa, nombre, instance_name, numero, provider, api_url, api_key, estado, fecha_creacion, fecha_actualizacion FROM whatsapp_instancia WHERE id_empresa = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            WhatsappInstancia instancia = new WhatsappInstancia();
            instancia.setIdWhatsappInstancia(rs.getLong("id_whatsapp_instancia"));
            instancia.setIdEmpresa(rs.getLong("id_empresa"));
            instancia.setNombre(rs.getString("nombre"));
            instancia.setInstanceName(rs.getString("instance_name"));
            instancia.setNumero(rs.getString("numero"));
            instancia.setProvider(rs.getString("provider"));
            instancia.setApiUrl(rs.getString("api_url"));
            instancia.setApiKey(rs.getString("api_key"));
            instancia.setEstado(rs.getString("estado"));
            instancia.setFechaCreacion(rs.getObject("fecha_creacion", java.time.OffsetDateTime.class));
            instancia.setFechaActualizacion(rs.getObject("fecha_actualizacion", java.time.OffsetDateTime.class));
            return instancia;
        }, idEmpresa);
    }
}