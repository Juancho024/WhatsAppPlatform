package com.jrdev.whatsappplatform.controller;

import com.jrdev.whatsappplatform.model.Integracion;
import com.jrdev.whatsappplatform.repository.IntegracionRepository;
import com.jrdev.whatsappplatform.service.IntegracionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/integraciones")
@RequiredArgsConstructor
public class IntegracionController {
    private final IntegracionService integracionService;
    private final IntegracionRepository integracionRepository;

    @GetMapping
    public List<Integracion> buscarTodos() {
        return integracionService.buscarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Integracion> buscarPorId(@PathVariable Long id) {
        Integracion integracion = integracionService.buscarPorId(id);
        if (integracion == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(integracion);
    }

    @PostMapping
    public ResponseEntity<Integracion> crear(@RequestBody Integracion integracion) {
        Integracion creado = integracionService.crear(integracion);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody Integracion integracion) {
        boolean actualizada = integracionService.actualizar(id, integracion);
        if (!actualizada) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/configurar")
    public ResponseEntity<String> configurarIntegracion(@RequestBody Integracion integracion) {
        try {
            Long idIntegracion = (long) integracionRepository.crear(integracion);
            return ResponseEntity.ok("✅ Integración '" + integracion.getTipo() + "' configurada con ID: " + idIntegracion);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Error guardando integración: " + e.getMessage());
        }
    }
}
