package com.jrdev.whatsappplatform.controller;

import com.jrdev.whatsappplatform.model.Empresa;
import com.jrdev.whatsappplatform.repository.EmpresaRepository;
import com.jrdev.whatsappplatform.repository.UsuarioEmpresaRepository;
import com.jrdev.whatsappplatform.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;
    private final EmpresaRepository empresaRepository;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository;

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
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody Empresa empresa) {
        boolean actualizada = empresaService.actualizar(id, empresa);
        if (!actualizada) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<String> registrarEmpresa(@RequestBody Empresa empresa, jakarta.servlet.http.HttpServletRequest request) {
        try {
            // Sacamos el ID 100% seguro que el JwtFilter preparó para nosotros
            Long idUsuario = (Long) request.getAttribute("idUsuarioAutenticado");

            // Se lo pasamos a tu servicio
            Long idGenerado = empresaService.crearEmpresaYVincular(empresa, idUsuario);

            return ResponseEntity.ok("✅ Empresa registrada y vinculada a ti como DUEÑO exitosamente con ID: " + idGenerado);
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
}