package com.jrdev.whatsappplatform.repository;

import com.jrdev.whatsappplatform.model.Empresa;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class EmpresaRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmpresaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Empresa> buscarTodas() {
        String sql = "SELECT id_empresa, nombre, identificacion, email, telefono, estado, fecha_creacion, fecha_actualizacion FROM empresa ORDER BY id_empresa";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Empresa empresa = new Empresa();
            empresa.setIdEmpresa(rs.getLong("id_empresa"));
            empresa.setNombre(rs.getString("nombre"));
            empresa.setIdentificacion(rs.getString("identificacion"));
            empresa.setEmail(rs.getString("email"));
            empresa.setTelefono(rs.getString("telefono"));
            empresa.setEstado(rs.getString("estado"));
            if (rs.getObject("fecha_creacion") != null) {
                empresa.setFechaCreacion(rs.getObject("fecha_creacion", java.time.OffsetDateTime.class));
            }

            if (rs.getObject("fecha_actualizacion") != null) {
                empresa.setFechaActualizacion(rs.getObject("fecha_actualizacion", java.time.OffsetDateTime.class));
            }

            return empresa;
        });
    }

    public Optional<Empresa> buscarPorId(Long id) {
        String sql = "SELECT id_empresa, nombre, identificacion, email, telefono, estado, fecha_creacion, fecha_actualizacion FROM empresa WHERE id_empresa = ?";
        List<Empresa> resultados = jdbcTemplate.query(sql, (rs, rowNum) -> new Empresa(rs.getLong("id_empresa"), rs.getString("nombre"), rs.getString("identificacion"), rs.getString("email"), rs.getString("telefono"), rs.getString("estado"), rs.getObject("fecha_creacion", java.time.OffsetDateTime.class), rs.getObject("fecha_actualizacion", java.time.OffsetDateTime.class)), id);
        return resultados.stream().findFirst();
    }

    public Long crear(Empresa empresa) {
        String sql = "INSERT INTO empresa (nombre, identificacion, email, telefono, estado) VALUES (?, ?, ?, ?, ?) RETURNING id_empresa";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, empresa.getNombre());
            ps.setString(2, empresa.getIdentificacion());
            ps.setString(3, empresa.getEmail());
            ps.setString(4, empresa.getTelefono());
            ps.setString(5, empresa.getEstado() != null ? empresa.getEstado() : "ACTIVA");
            return ps;
        }, keyHolder);

        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }

    public int actualizar(Long id, Empresa empresa) {
        String sql = "UPDATE empresa SET nombre = ?, identificacion = ?, email = ?, telefono = ?, estado = ?, fecha_actualizacion = CURRENT_TIMESTAMP WHERE id_empresa = ?";
        return jdbcTemplate.update(sql,
                empresa.getNombre(),
                empresa.getIdentificacion(),
                empresa.getEmail(),
                empresa.getTelefono(),
                empresa.getEstado(),
                id);
    }

    public int eliminar(Long id) {
        String sql = "DELETE FROM empresa WHERE id_empresa = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int cambiarEstado(Long id, String estado) {
        String sql = "UPDATE empresa SET estado = ?, fecha_actualizacion = CURRENT_TIMESTAMP WHERE id_empresa = ?";
        return jdbcTemplate.update(sql, estado, id);
    }
}