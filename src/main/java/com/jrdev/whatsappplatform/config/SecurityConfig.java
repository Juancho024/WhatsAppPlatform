package com.jrdev.whatsappplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 🔥 Configuración del muro de seguridad (Filtros)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Permite conexiones externas
                .csrf(csrf -> csrf.disable()) // Desactiva la protección de formularios tradicionales (usamos JWT)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Por ahora permitimos todo para que el CORS pase
                );

        return http.build();
    }

    // 3. 🔥 Las reglas del CORS: Quién puede entrar y qué puede hacer
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permite solicitudes desde cualquier lado (ideal para desarrollo).
        // En producción puedes cambiar "* " por "http://localhost:5174" o tu dominio real.
        configuration.setAllowedOriginPatterns(List.of("*"));

        // Métodos permitidos (GET, POST, PUT, DELETE y el maldito OPTIONS que causó el error)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Cabeceras permitidas (React manda Content-Type y Authorization con el token)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Permite enviar credenciales
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica esto a TODAS las rutas (/api/**)
        return source;
    }
}