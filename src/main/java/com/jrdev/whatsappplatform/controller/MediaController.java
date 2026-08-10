package com.jrdev.whatsappplatform.controller;


import com.jrdev.whatsappplatform.model.Media;
import com.jrdev.whatsappplatform.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;

    @GetMapping
    public List<Media> obtenerMedia() {
        return mediaService.buscarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Media> buscarPorId(@PathVariable Long id) {
        Media media = mediaService.buscarPorId(id);
        if (media == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(media);
    }

    @PostMapping
    public ResponseEntity<Media> crear(@RequestBody Media media) {
        Media creado = mediaService.crear(media);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody Media media) {
        boolean actualizada = mediaService.actualizar(id, media);
        if (!actualizada) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

}
