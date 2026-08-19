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

        // 1. ZONAS PÚBLICAS (No piden token)
        // Dejamos pasar el login, el registro y los webhooks de Evolution
        if (path.startsWith("/api/auth/") || path.startsWith("/api/webhooks/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Si es una petición OPTIONS (Preflight de CORS de React), la dejamos pasar
        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. REVISAR LA CABECERA (Header)
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Acceso denegado. Falta el Token JWT en la cabecera 'Authorization'.\"}");
            return;
        }

        // 3. EXTRAER Y VALIDAR EL TOKEN
        String token = authHeader.substring(7); // Quitamos la palabra "Bearer " (7 caracteres)

        if (!jwtService.validarToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"El Token es invalido o ya ha expirado.\"}");
            return;
        }

        // 🔥 4. EL TRUCO MAESTRO: Extraemos el ID del usuario del token
        // y lo guardamos en la petición. Así los controladores sabrán quién es
        // sin tener que confiar en lo que manda React.
        Long idUsuario = jwtService.extraerIdUsuario(token);
        request.setAttribute("idUsuarioAutenticado", idUsuario);

        // 5. ¡Todo nítido! Lo dejamos pasar al controlador
        filterChain.doFilter(request, response);
    }
}