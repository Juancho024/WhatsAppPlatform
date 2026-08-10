package com.jrdev.whatsappplatform.controller;

import com.jrdev.whatsappplatform.model.Chat;
import com.jrdev.whatsappplatform.model.Mensaje;
import com.jrdev.whatsappplatform.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping
    public List<Chat> buscarTodos() {
        return chatService.buscarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Chat> buscarPorId(@PathVariable Long id) {
        Chat chat = chatService.buscarPorId(id);
        if (chat == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(chat);
    }

    @PostMapping
    public ResponseEntity<Chat> crear(@RequestBody Chat chat) {
        Chat creado = chatService.crear(chat);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody Chat chat) {
        boolean actualizada = chatService.actualizar(id, chat);
        if (!actualizada) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/abrir")
    public ResponseEntity<Void> abrirChat(@PathVariable Long id) {
        chatService.abrirChat(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<Void> cerrarChat(@PathVariable Long id) {
        chatService.cerrarChat(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/bloquear")
    public ResponseEntity<Void> bloquearChat(@PathVariable Long id) {
        chatService.bloquearChat(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/mensajes")
    public ResponseEntity<List<Mensaje>> buscarMensajes(
            @PathVariable Long id) {

        List<Mensaje> mensajes = chatService.buscarMensajes(id);

        return ResponseEntity.ok(mensajes);
    }

}
