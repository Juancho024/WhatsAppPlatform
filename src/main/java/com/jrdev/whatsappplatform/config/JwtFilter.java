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

        String path = request.getRequestURI();

        // 1. ZONAS PÚBLICAS
        if (path.startsWith("/api/auth/") || path.startsWith("/api/webhooks/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. DEJAR PASAR EL PREFLIGHT DE CORS (OPTIONS) SIEMPRE
        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 3. REVISAR LA CABECERA
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            rechazarPeticion(response, "Acceso denegado. Falta el Token JWT.");
            return;
        }

        // 4. EXTRAER Y VALIDAR EL TOKEN
        String token = authHeader.substring(7);

        if (!jwtService.validarToken(token)) {
            rechazarPeticion(response, "El Token es invalido o ya ha expirado.");
            return;
        }

        // 5. GUARDAR ID Y DEJAR PASAR
        Long idUsuario = jwtService.extraerIdUsuario(token);
        request.setAttribute("idUsuarioAutenticado", idUsuario);
        filterChain.doFilter(request, response);
    }

    // 🔥 FUNCIÓN MÁGICA PARA RECHAZAR SIN ROMPER EL CORS DE REACT 🔥
    private void rechazarPeticion(HttpServletResponse response, String mensaje) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + mensaje + "\"}");
    }
}