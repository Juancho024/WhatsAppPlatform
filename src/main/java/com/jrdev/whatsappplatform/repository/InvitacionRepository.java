package com.jrdev.whatsappplatform.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class InvitacionRepository {

    private final JdbcTemplate jdbcTemplate;

    public InvitacionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void guardarInvitacion(Long idEmpresa, String correo, String rol, String token) {
        String sql = "INSERT INTO invitacion (id_empresa, correo_invitado, rol_asignado, token, estado) VALUES (?, ?, ?, ?, 'PENDIENTE')";
        jdbcTemplate.update(sql, idEmpresa, correo, rol, token);
    }

    public Optional<Map<String, Object>> buscarPorToken(String token) {
        String sql = "SELECT * FROM invitacion WHERE token = ?";
        try {
            return Optional.of(jdbcTemplate.queryForMap(sql, token));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void actualizarEstado(String token, String estado) {
        String sql = "UPDATE invitacion SET estado = ? WHERE token = ?";
        jdbcTemplate.update(sql, estado, token);
    }
}