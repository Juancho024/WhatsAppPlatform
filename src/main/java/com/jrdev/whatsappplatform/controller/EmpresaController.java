package com.jrdev.whatsappplatform.controller;

import com.jrdev.whatsappplatform.dto.InvitarMiembroDto;
import com.jrdev.whatsappplatform.model.Empresa;
import com.jrdev.whatsappplatform.model.Usuario;
import com.jrdev.whatsappplatform.model.UsuarioEmpresa;
import com.jrdev.whatsappplatform.repository.EmpresaRepository;
import com.jrdev.whatsappplatform.repository.UsuarioEmpresaRepository;
import com.jrdev.whatsappplatform.repository.UsuarioRepository;
import com.jrdev.whatsappplatform.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;
    private final EmpresaRepository empresaRepository;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository;
    private final UsuarioRepository usuarioRepository;

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
    public ResponseEntity<Object> invitarMiembro(
            @PathVariable Long idEmpresa,
            @RequestBody InvitarMiembroDto request) {
        try {
            // 1. Buscamos si el correo pertenece a un usuario (Usando tu JPA de Usuario)
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(request.getEmail());

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("El usuario con el correo " + request.getEmail() + " no tiene cuenta en la plataforma. Pídele que se registre primero.");
            }

            Usuario usuarioEncontrado = usuarioOpt.get();
            Long idUsuario = usuarioEncontrado.getIdUsuario();

            // 2. Verificamos que la empresa exista (Usando tu método buscarPorId de JdbcTemplate)
            Optional<Empresa> empresaOpt = empresaRepository.buscarPorId(idEmpresa);
            if (empresaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("La empresa no existe.");
            }

            // 3. Verificamos si ya está vinculado (Usando nuestro nuevo método JDBC)
            boolean yaVinculado = usuarioEmpresaRepository.existeVinculo(idUsuario, idEmpresa);

            if (yaVinculado) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Este usuario ya es miembro de la organización.");
            }

            // 4. Lo vinculamos insertándolo directo con SQL (Usando nuestro nuevo método JDBC)
            usuarioEmpresaRepository.vincularUsuario(idUsuario, idEmpresa, request.getRol_empresa());

            return ResponseEntity.ok("Miembro vinculado exitosamente a la empresa.");

        } catch (Exception e) {
            System.err.println("Error al invitar miembro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando la invitación.");
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