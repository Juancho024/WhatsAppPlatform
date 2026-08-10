package com.jrdev.whatsappplatform.controller;

import com.jrdev.whatsappplatform.model.Evento;
import com.jrdev.whatsappplatform.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {
    private final EventoService eventoService;

    @GetMapping
    public List<Evento> obtenerEventos() {
        return eventoService.buscarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evento> buscarPorId(@PathVariable Long id) {
        Evento evento = eventoService.buscarPorId(id);
        if (evento == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(evento);
    }

    @PostMapping
    public ResponseEntity<Evento> crear(@RequestBody Evento evento) {
        Evento creado = eventoService.crear(evento);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody Evento evento) {
        boolean actualizada = eventoService.actualizar(id, evento);
        if (!actualizada) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/procesar")
    public ResponseEntity<Void> marcarProcesado(@PathVariable Long id) {
        boolean actualizado = eventoService.marcarProcesado(id);
        if (!actualizado) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/error")
    public ResponseEntity<Void> marcarError(@PathVariable Long id, @RequestParam String error) {
        boolean actualizado = eventoService.marcarError(id, error);
        if (!actualizado) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
