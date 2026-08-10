package com.jrdev.whatsappplatform.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestDatabaseController {

    private final JdbcTemplate jdbcTemplate;

    public TestDatabaseController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/test-db")
    public String testDatabase() {

        Integer resultado =
                jdbcTemplate.queryForObject(
                        "SELECT 1",
                        Integer.class
                );

        return "Base de datos conectada: " + resultado;
    }
}