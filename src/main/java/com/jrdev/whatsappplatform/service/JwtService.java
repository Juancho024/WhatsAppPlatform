package com.jrdev.whatsappplatform.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    // 🔥 CAMBIO CLAVE: Una llave de 256 bits estática. ¡Nunca cambiará aunque el servidor se reinicie!
    private final String LLAVE_SECRETA = "4qhq8L6H9A3yR2uF5wE8xG1vM7nJ0pC3tV6zB9yD4qE=";

    private final Key SECRET_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(LLAVE_SECRETA));

    // El token durará 24 horas (en milisegundos)
    private final long EXPIRATION_TIME = 86400000;

    public String generarToken(Long idUsuario, String correo) {
        return Jwts.builder()
                .setSubject(correo)
                .claim("idUsuario", idUsuario)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public Long extraerIdUsuario(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("idUsuario", Long.class);
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}