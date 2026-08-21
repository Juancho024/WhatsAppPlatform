package com.jrdev.whatsappplatform.controller;

import com.jrdev.whatsappplatform.dto.ActualizarUsuarioDto;
import com.jrdev.whatsappplatform.dto.PreferenciaDto;
import com.jrdev.whatsappplatform.model.Preferencia;
import com.jrdev.whatsappplatform.model.Usuario;
import com.jrdev.whatsappplatform.repository.PreferenciaRepository;
import com.jrdev.whatsappplatform.repository.UsuarioRepository;
import com.jrdev.whatsappplatform.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private PreferenciaRepository preferenciaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private UsuarioService usuarioService;

    @PutMapping("/{id}")
    public ResponseEntity<Object> actualizar(@PathVariable Long id, @RequestBody ActualizarUsuarioDto request) {
        try {
            // 1. Buscamos el usuario por su ID
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

            // 2. Actualizamos solo los campos de perfil
            usuario.setNombreCompleto(request.getNombreCompleto());
            usuario.setUsuario(request.getUsuario());
            usuario.setCorreo(request.getCorreo());

            // 3. Guardamos los cambios
            usuarioRepository.save(usuario);

            return ResponseEntity.ok("Perfil actualizado exitosamente.");

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error al actualizar perfil: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al actualizar.");
        }
    }

    @PutMapping("/preferencias")
    public ResponseEntity<Object> actualizarPreferencias(@RequestBody PreferenciaDto request, Principal principal) {
        try {
            String username = principal.getName();
            Usuario usuario = usuarioRepository.findByUsuario(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

            // Buscamos si el usuario ya tiene preferencias, si no, creamos una nueva instancia
            Preferencia pref = preferenciaRepository.findByUsuario(usuario)
                    .orElse(new Preferencia());

            // Actualizamos los campos
            pref.setUsuario(usuario); // Asociamos la preferencia al usuario
            pref.setLanguage(request.getLanguage());
            pref.setTheme(request.getTheme());
            pref.setEmailNotifications(request.isEmailNotifications());
            pref.setPushNotifications(request.isPushNotifications());
            pref.setTwoFactorAuth(request.isTwoFactorAuth());

            // Guardamos en la tabla de preferencias
            preferenciaRepository.save(pref);

            return ResponseEntity.ok("Preferencias guardadas en la BD.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/cuenta")
    public ResponseEntity<Object> eliminarCuentaCompleta(Principal principal) {
        try {
            // Obtenemos el usuario autenticado por el Token JWT
            String username = principal.getName();
            Usuario usuario = usuarioRepository.findByUsuario(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

            // Llamamos al servicio para ejecutar el borrado masivo
            usuarioService.eliminarUsuarioYSuEcosistema(usuario.getIdUsuario());

            return ResponseEntity.noContent().build(); // Devuelve 204 (Éxito sin contenido)

        } catch (Exception e) {
            System.err.println("Error crítico al borrar cuenta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No se pudo eliminar la cuenta de forma segura.");
        }
    }
}
