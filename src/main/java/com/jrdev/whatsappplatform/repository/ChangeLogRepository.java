package com.jrdev.whatsappplatform.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ChangeLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public ChangeLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void guardarCommit(String hash, String mensaje, String autor, String fechaCommit) {
        String sql = "INSERT INTO changelog (commit_hash, mensaje, autor, fecha_commit) VALUES (?, ?, ?, CAST(? AS TIMESTAMPTZ)) ON CONFLICT (commit_hash) DO NOTHING";
        jdbcTemplate.update(sql, hash, mensaje, autor, fechaCommit);
    }
}