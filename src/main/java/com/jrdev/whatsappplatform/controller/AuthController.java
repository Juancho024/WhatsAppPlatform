package com.jrdev.whatsappplatform.controller;

import com.jrdev.whatsappplatform.dto.LoginRequestDto;
import com.jrdev.whatsappplatform.dto.RegistroRequestDto;
import com.jrdev.whatsappplatform.model.Usuario;
import com.jrdev.whatsappplatform.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Fundamental para evitar errores de CORS con React
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    @PostMapping("/registro")
    public ResponseEntity<String> registrarUsuario(@RequestBody RegistroRequestDto request) {
        try {
            // 1. Validaciones de seguridad básicas
            if (usuarioRepository.existsByCorreo(request.getCorreo())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya está registrado.");
            }
            if (usuarioRepository.existsByUsuario(request.getUsuario())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario no está disponible.");
            }

            // 2. Encriptar la contraseña (¡Nunca en texto plano!)
            String salt = BCrypt.gensalt(12); // Nivel de seguridad alto
            String hashPassword = BCrypt.hashpw(request.getPassword(), salt);

            // 3. Crear y guardar el nuevo usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombreCompleto(request.getNombreCompleto());
            nuevoUsuario.setUsuario(request.getUsuario());
            nuevoUsuario.setCorreo(request.getCorreo());
            nuevoUsuario.setPasswordHash(hashPassword);

            usuarioRepository.save(nuevoUsuario);

            return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado con éxito.");

        } catch (Exception e) {
            System.err.println("Error en el registro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el registro.");
        }
    }

    @PostMapping("/iniciar")
    public ResponseEntity<String> registrarUsuario(@RequestBody LoginRequestDto request){
        try {
            // 1. Buscar el usuario por nombre de usuario
            var usuarioOpt = usuarioRepository.findByUsuario(request.getUsuario());
            // Si el usuario no existe, devolvemos error 401
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas.");
            }

            Usuario usuario = usuarioOpt.get();

            // 2. Verificamos la contraseña encriptada
            boolean passwordCorrecta = BCrypt.checkpw(request.getPassword(), usuario.getPasswordHash());

            if (!passwordCorrecta) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas.");
            }

            // 3. ¡Login exitoso! Creamos el JSON con los datos del usuario para el Header de React
            java.util.Map<String, Object> respuesta = new java.util.HashMap<>();
            respuesta.put("mensaje", "Inicio de sesión exitoso");
            respuesta.put("nombreCompleto", usuario.getNombreCompleto());
            respuesta.put("correo", usuario.getCorreo());
            respuesta.put("rol", usuario.getRol());

            return ResponseEntity.ok(respuesta.toString());
        } catch (Exception e) {
            System.err.println("Error en el inicio de sesión: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el inicio de sesión.");
        }
    }


}