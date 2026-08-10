package com.jrdev.whatsappplatform.controller;


import com.jrdev.whatsappplatform.model.Contacto;
import com.jrdev.whatsappplatform.service.ContactoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contactos")
@RequiredArgsConstructor
public class ContactoController {
    private final ContactoService contactoService;

    @GetMapping
    public List<Contacto> buscarTodos() {
        return contactoService.buscarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contacto> buscarPorId(@PathVariable Long id) {
        Contacto contacto = contactoService.buscarPorId(id);
        if (contacto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(contacto);
    }

    @PostMapping
    public ResponseEntity<Contacto> crear(@RequestBody Contacto contacto) {
        Contacto creado = contactoService.crear(contacto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody Contacto contacto) {
        boolean actualizada = contactoService.actualizar(id, contacto);
        if (!actualizada) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id) {
        boolean cambiado = contactoService.cambiarEstado(id);
        if (!cambiado) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
