package com.jrdev.whatsappplatform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/changelogs")
public class ChangeLogController {

    private final JdbcTemplate jdbcTemplate;

    public ChangeLogController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> obtenerChangelogs() {
        try {
            // Traemos los últimos 20 commits ordenados por fecha descendente
            String sql = "SELECT id_changelog, commit_hash, mensaje, autor, fecha_commit FROM changelog ORDER BY fecha_commit DESC LIMIT 20";
            List<Map<String, Object>> logs = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            System.err.println("Error al obtener changelogs: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}