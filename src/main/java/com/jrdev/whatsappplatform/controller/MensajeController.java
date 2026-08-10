package com.jrdev.whatsappplatform.controller;

import com.jrdev.whatsappplatform.model.Mensaje;
import com.jrdev.whatsappplatform.service.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
@RequiredArgsConstructor
public class MensajeController {
    private final MensajeService mensajeService;

    @GetMapping
    public List<Mensaje> obtenerMensajes() {
        return mensajeService.buscarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mensaje> buscarPorId(@PathVariable Long id) {
        Mensaje mensaje = mensajeService.buscarPorId(id);
        if (mensaje == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mensaje);
    }

    @PostMapping
    public ResponseEntity<Mensaje> crear(@RequestBody Mensaje mensaje) {
        Mensaje creado = mensajeService.crear(mensaje);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody Mensaje mensaje) {
        boolean actualizada = mensajeService.actualizar(id, mensaje);
        if (!actualizada) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/chat/{idChat}")
    public ResponseEntity<Mensaje> crearEnChat(@PathVariable Long idChat, @RequestBody Mensaje mensaje) {
        Mensaje creado = mensajeService.crearEnChat(idChat, mensaje);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
