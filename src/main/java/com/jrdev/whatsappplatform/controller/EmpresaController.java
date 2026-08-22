package com.jrdev.whatsappplatform.controller;

import com.jrdev.whatsappplatform.dto.InvitarMiembroDto;
import com.jrdev.whatsappplatform.model.Empresa;
import com.jrdev.whatsappplatform.model.Usuario;
import com.jrdev.whatsappplatform.model.UsuarioEmpresa;
import com.jrdev.whatsappplatform.repository.EmpresaRepository;
import com.jrdev.whatsappplatform.repository.InvitacionRepository;
import com.jrdev.whatsappplatform.repository.UsuarioEmpresaRepository;
import com.jrdev.whatsappplatform.repository.UsuarioRepository;
import com.jrdev.whatsappplatform.service.EmailService;
import com.jrdev.whatsappplatform.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;
    private final EmpresaRepository empresaRepository;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final InvitacionRepository invitacionRepository;
    private final EmailService emailService;

    @GetMapping
    public List<Empresa> obtenerEmpresas() {
        return empresaService.buscarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> buscarPorId(@PathVariable Long id) {
        Empresa empresa = empresaService.buscarPorId(id);
        if (empresa == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(empresa);
    }

    @PostMapping
    public ResponseEntity<Empresa> crear(@RequestBody Empresa empresa) {
        Empresa creada = empresaService.crear(empresa);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> payload, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Long idUsuario = (Long) request.getAttribute("idUsuarioAutenticado");

            Empresa empresa = new Empresa();
            empresa.setNombre((String) payload.get("nombre"));
            empresa.setIdentificacion((String) payload.get("identificacion"));
            empresa.setEmail((String) payload.get("email"));
            empresa.setTelefono((String) payload.get("telefono"));
            empresa.setEstado((String) payload.get("estado"));

            String nuevoRol = (String) payload.get("rol_empresa");

            boolean actualizada = empresaService.actualizarEmpresaYRol(id, empresa, idUsuario, nuevoRol);
            if (!actualizada) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        boolean eliminada = empresaService.eliminar(id);
        if (!eliminada) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        empresaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        empresaService.activar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/registrar")
    public ResponseEntity<String> registrarEmpresa(@RequestBody Map<String, Object> payload, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Long idUsuario = (Long) request.getAttribute("idUsuarioAutenticado");

            // Mapeamos los datos de la empresa desde el payload recibido de React
            Empresa empresa = new Empresa();
            empresa.setNombre((String) payload.get("nombre"));
            empresa.setIdentificacion((String) payload.get("identificacion"));
            empresa.setEmail((String) payload.get("email"));
            empresa.setTelefono((String) payload.get("telefono"));
            empresa.setEstado((String) payload.get("estado"));

            // 🔥 Capturamos el rol que seleccionó en el combobox (por defecto DUEÑO si viene vacío)
            String rolEmpresa = (String) payload.getOrDefault("rol_empresa", "DUEÑO");

            // Se lo pasamos al servicio junto al rol elegido
            Long idGenerado = empresaService.crearEmpresaYVincular(empresa, idUsuario, rolEmpresa);

            return ResponseEntity.ok("✅ Empresa registrada y vinculada exitosamente con ID: " + idGenerado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Error al registrar la empresa: " + e.getMessage());
        }
    }

    @GetMapping("/mis-empresas")
    public ResponseEntity<List<Map<String, Object>>> obtenerMisEmpresas(jakarta.servlet.http.HttpServletRequest request) {
        System.out.println("========== DEBUG /mis-empresas ==========");
        try {
            Long idUsuario = (Long) request.getAttribute("idUsuarioAutenticado");
            System.out.println("El Filtro JWT dejó pasar al Usuario ID: " + idUsuario);

            List<Map<String, Object>> misEmpresas = usuarioEmpresaRepository.obtenerEmpresasPorUsuario(idUsuario);
            System.out.println("Devolviendo a React " + misEmpresas.size() + " empresas.");
            System.out.println("=========================================");

            return ResponseEntity.ok(misEmpresas);
        } catch (Exception e) {
            System.out.println("❌ ERROR EXPLOSIVO EN /mis-empresas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{idEmpresa}/invitar")
    public ResponseEntity<Object> invitarMiembro(@PathVariable Long idEmpresa, @RequestBody InvitarMiembroDto request) {
        try {
            String correoDestino = request.getEmail();

            // 1. Verificamos que la empresa exista
            Optional<Empresa> empresaOpt = empresaRepository.buscarPorId(idEmpresa);
            if (empresaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La empresa no existe.");
            }

            // 2. Verificamos si el correo ya es de un usuario registrado para evitar invitar a alguien que ya está dentro
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correoDestino);

            if (usuarioOpt.isPresent()) {
                boolean yaVinculado = usuarioEmpresaRepository.existeVinculo(usuarioOpt.get().getIdUsuario(), idEmpresa);
                if (yaVinculado) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Este usuario ya es miembro de la organización.");
                }
            }

            // 3. Generamos el token y guardamos la invitación (¡sin importar si existe en la plataforma o no!)
            String token = UUID.randomUUID().toString();
            invitacionRepository.guardarInvitacion(idEmpresa, correoDestino, request.getRol_empresa(), token);

            // 4. Enviamos el correo con el link
            emailService.enviarInvitacion(correoDestino, empresaOpt.get().getNombre(), token);

            return ResponseEntity.ok("Invitación enviada por correo exitosamente.");

        } catch (Exception e) {
            System.err.println("Error al invitar miembro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando la invitación.");
        }
    }

    @PostMapping("/aceptar-invitacion")
    public ResponseEntity<Object> aceptarInvitacion(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String username = request.get("usuario"); // 🔥 Ahora lo sacamos del JSON del Frontend

            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token no proporcionado.");
            }

            if (username == null || username.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario no identificado en la sesión.");
            }

            // 1. Buscamos la invitación
            Optional<Map<String, Object>> invitacionOpt = invitacionRepository.buscarPorToken(token);
            if (invitacionOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invitación no válida o no existe.");
            }

            Map<String, Object> invitacion = invitacionOpt.get();

            // 2. Verificamos que esté pendiente
            if (!"PENDIENTE".equals(invitacion.get("estado"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Esta invitación ya fue procesada o cancelada.");
            }

            // 3. Buscamos al usuario usando el username que nos mandó React
            Usuario usuarioLogueado = usuarioRepository.findByUsuario(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la base de datos."));

            Long idEmpresa = ((Number) invitacion.get("id_empresa")).longValue();
            String rolAsignado = (String) invitacion.get("rol_asignado");

            // 4. Vinculamos la cuenta
            if (!usuarioEmpresaRepository.existeVinculo(usuarioLogueado.getIdUsuario(), idEmpresa)) {
                usuarioEmpresaRepository.vincularUsuario(usuarioLogueado.getIdUsuario(), idEmpresa, rolAsignado);
            }

            // 5. Marcamos la invitación como ACEPTADA
            invitacionRepository.actualizarEstado(token, "ACEPTADA");

            return ResponseEntity.ok("¡Bienvenido al equipo! Invitación vinculada a tu cuenta con éxito.");

        } catch (Exception e) {
            System.err.println("Error procesando la aceptación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al aceptar la invitación.");
        }
    }

    @GetMapping("/{idEmpresa}/miembros")
    public ResponseEntity<Object> obtenerMiembros(@PathVariable Long idEmpresa, Principal principal) {
        try {
            // (Opcional pero recomendado) Aquí podrías validar si el usuario logueado
            // (principal.getName()) tiene permiso para ver esta empresa.

            // Ejecutamos la consulta
            List<Map<String, Object>> miembros = usuarioEmpresaRepository.obtenerMiembrosDeEmpresa(idEmpresa);

            // Devolvemos la lista directamente. Spring Boot la convierte al JSON exacto que espera React
            return ResponseEntity.ok(miembros);

        } catch (Exception e) {
            System.err.println("Error al obtener los miembros de la empresa: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando la solicitud.");
        }
    }
}