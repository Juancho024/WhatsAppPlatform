package com.jrdev.whatsappplatform.config;

import com.jrdev.whatsappplatform.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 🔥 SIEMPRE INCLUIR ESTAS CABECERAS PARA EVITAR BLOQUEOS DE CORS
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With");
        response.setHeader("Access-Control-Max-Age", "3600");

        // Si el navegador manda una petición de prueba (OPTIONS), la aprobamos de inmediato
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String path = request.getRequestURI();

        // 1. ZONAS PÚBLICAS
        if (path.startsWith("/api/auth/") || path.startsWith("/api/webhooks/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. REVISAR LA CABECERA DE AUTORIZACIÓN
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Falta el Token JWT.\"}");
            return;
        }

        // 3. EXTRAER Y VALIDAR EL TOKEN
        String token = authHeader.substring(7);

        if (!jwtService.validarToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token invalido o expirado.\"}");
            return;
        }

        // 4. GUARDAR ID Y DEJAR PASAR
        Long idUsuario = jwtService.extraerIdUsuario(token);
        request.setAttribute("idUsuarioAutenticado", idUsuario);
        filterChain.doFilter(request, response);
    }
}