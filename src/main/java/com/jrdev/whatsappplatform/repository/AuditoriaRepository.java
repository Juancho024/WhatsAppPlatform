package com.jrdev.whatsappplatform.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditoriaRepository {

    private final JdbcTemplate jdbcTemplate;

    public void registrarAccion(Long idEmpresa, Long idUsuario, String accion, String tablaAfectada, Long registroId, String detallesJson) {
        String sql = "INSERT INTO auditoria_seguridad (id_empresa, id_usuario, accion, tabla_afectada, registro_id, detalles) VALUES (?, ?, ?, ?, ?, ?::jsonb)";
        jdbcTemplate.update(sql, idEmpresa, idUsuario, accion, tablaAfectada, registroId, detallesJson);
    }
}