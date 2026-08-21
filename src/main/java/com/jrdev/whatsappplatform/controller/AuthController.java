package com.jrdev.whatsappplatform.controller;

import com.jrdev.whatsappplatform.dto.LoginRequestDto;
import com.jrdev.whatsappplatform.dto.RegistroRequestDto;
import com.jrdev.whatsappplatform.model.Usuario;
import com.jrdev.whatsappplatform.repository.UsuarioRepository;
import com.jrdev.whatsappplatform.repository.UsuarioEmpresaRepository; // <-- Importamos tu nuevo repo
import com.jrdev.whatsappplatform.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository;
    private final JwtService jwtService;

    @PostMapping("/registro")
    public ResponseEntity<String> registrarUsuario(@RequestBody RegistroRequestDto request) {
        try {
            if (usuarioRepository.existsByCorreo(request.getCorreo())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya está registrado.");
            }
            if (usuarioRepository.existsByUsuario(request.getUsuario())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario no está disponible.");
            }

            String salt = BCrypt.gensalt(12);
            String hashPassword = BCrypt.hashpw(request.getPassword(), salt);

            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombreCompleto(request.getNombreCompleto());
            nuevoUsuario.setUsuario(request.getUsuario());
            nuevoUsuario.setCorreo(request.getCorreo());
            nuevoUsuario.setPasswordHash(hashPassword);

            usuarioRepository.save(nuevoUsuario);

            // NOTA: Cuando un usuario se registra por primera vez, no tiene empresas.
            // Más adelante tendrás que hacer un endpoint para que cree su primera empresa
            // y se inserte en la tabla usuario_empresa como DUEÑO.

            return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado con éxito.");

        } catch (Exception e) {
            System.err.println("Error en el registro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el registro.");
        }
    }

    @PostMapping("/iniciar")
    public ResponseEntity<Object> iniciarSesion(@RequestBody LoginRequestDto request){
        try {
            var usuarioOpt = usuarioRepository.findByUsuario(request.getUsuario());
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas.");
            }

            Usuario usuario = usuarioOpt.get();

            boolean passwordCorrecta = BCrypt.checkpw(request.getPassword(), usuario.getPasswordHash());

            if (!passwordCorrecta) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas.");
            }

            // 🔥 AQUÍ ESTÁ LA MAGIA DEL SAAS 🔥
            // Buscamos todas las empresas donde este usuario tiene poder
            List<Map<String, Object>> empresasVinculadas = usuarioEmpresaRepository.obtenerEmpresasPorUsuario(usuario.getIdUsuario());

            System.out.println("========== DEBUG LOGIN ==========");
            System.out.println("Usuario logueado: " + usuario.getUsuario() + " (ID: " + usuario.getIdUsuario() + ")");
            System.out.println("Empresas encontradas en la BD: " + empresasVinculadas.size());
            System.out.println("Detalle empresas: " + empresasVinculadas);
            System.out.println("=================================");

            // 🔥 CREAMOS EL TOKEN (LA PULSERA VIP)
            String token = jwtService.generarToken(usuario.getIdUsuario(), usuario.getCorreo());

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Inicio de sesión exitoso");
            respuesta.put("token", token);
            respuesta.put("idUsuario", usuario.getIdUsuario());
            respuesta.put("usuario", usuario.getUsuario());
            respuesta.put("nombreCompleto", usuario.getNombreCompleto());
            respuesta.put("correo", usuario.getCorreo());
            respuesta.put("empresas", empresasVinculadas);

            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            System.err.println("Error en el inicio de sesión: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el inicio de sesión.");
        }
    }
}