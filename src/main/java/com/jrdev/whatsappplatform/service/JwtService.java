package com.jrdev.whatsappplatform.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    // ⚠️ Esta es tu firma secreta. En producción esto DEBE ir en tu application.properties.
    // Por ahora usamos una llave segura generada al azar cada vez que arranca el servidor.
    private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // El token durará 24 horas (en milisegundos)
    private final long EXPIRATION_TIME = 86400000;

    // 1. Método para CREAR el token cuando el usuario hace login
    public String generarToken(Long idUsuario, String correo) {
        return Jwts.builder()
                .setSubject(correo) // A quién le pertenece
                .claim("idUsuario", idUsuario) // Guardamos el ID de forma segura adentro
                .setIssuedAt(new Date()) // Fecha de creación
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Fecha de muerte
                .signWith(SECRET_KEY) // Firmamos con el sello de la empresa
                .compact();
    }

    // 2. Método para LEER el ID del usuario desde el token (cuando hace una petición)
    public Long extraerIdUsuario(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("idUsuario", Long.class);
    }

    // 3. Método para verificar si la pulsera es falsa o ya expiró
    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false; // Si alguien lo modificó o ya caducó, explota y devuelve falso
        }
    }
}