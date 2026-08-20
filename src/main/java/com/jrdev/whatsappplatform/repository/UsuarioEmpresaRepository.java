package com.jrdev.whatsappplatform.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UsuarioEmpresaRepository {

    private final JdbcTemplate jdbcTemplate;

    // 1. Vincular un usuario a una empresa
    public void vincularUsuarioAEmpresa(Long idUsuario, Long idEmpresa, String rol) {
        String sql = "INSERT INTO usuario_empresa (id_usuario, id_empresa, rol_empresa, estado) VALUES (?, ?, ?, 'ACTIVO')";
        jdbcTemplate.update(sql, idUsuario, idEmpresa, rol);
    }

    // 2. LA MÁS IMPORTANTE: Verificar si el usuario tiene acceso a esa empresa
    public boolean tieneAcceso(Long idUsuario, Long idEmpresa) {
        String sql = "SELECT EXISTS (SELECT 1 FROM usuario_empresa WHERE id_usuario = ? AND id_empresa = ? AND estado = 'ACTIVO')";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, idUsuario, idEmpresa));
    }

    // 3. Saber qué rol tiene en esa empresa
    public String obtenerRolEnEmpresa(Long idUsuario, Long idEmpresa) {
        String sql = "SELECT rol_empresa FROM usuario_empresa WHERE id_usuario = ? AND id_empresa = ? AND estado = 'ACTIVO'";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, idUsuario, idEmpresa);
        } catch (Exception e) {
            return null; // No tiene acceso
        }
    }

    // Este método devuelve la lista de empresas y el rol que tiene el usuario en cada una
    public List<Map<String, Object>> obtenerEmpresasPorUsuario(Long idUsuario) {
        String sql = """
            SELECT e.id_empresa, e.nombre AS nombre_empresa, e.identificacion, e.email, e.telefono, e.estado, ue.rol_empresa 
            FROM empresa e 
            JOIN usuario_empresa ue ON e.id_empresa = ue.id_empresa 
            WHERE ue.id_usuario = ? AND ue.estado = 'ACTIVO' AND e.estado = 'ACTIVA'
            """;
        return jdbcTemplate.queryForList(sql, idUsuario);
    }
}