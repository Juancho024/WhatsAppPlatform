package com.jrdev.whatsappplatform.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class MusicStockDbConfig {

    // Le damos un nombre específico al Bean para no confundirlo con el de WhatsApp
    @Bean(name = "musicStockJdbcTemplate")
    public JdbcTemplate musicStockJdbcTemplate() {
        HikariConfig config = new HikariConfig();

        // Las credenciales exactas que me pasaste de MusicStock
        config.setJdbcUrl("jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:5432/postgres?user=postgres.mopslsqgukkgxkbxypja&password=leC4Jq6qTgid0Zf5");
        config.setUsername("postgres");
        config.setPassword("leC4Jq6qTgid0Zf5");

        // Optimizaciones ligeras porque el bot solo va a hacer consultas de lectura (SELECT)
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);

        DataSource dataSource = new HikariDataSource(config);
        return new JdbcTemplate(dataSource);
    }
}