package com.jrdev.whatsappplatform.service;


import com.jrdev.whatsappplatform.model.Chat;
import com.jrdev.whatsappplatform.model.Mensaje;
import com.jrdev.whatsappplatform.repository.ChatRepository;
import com.jrdev.whatsappplatform.repository.MensajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final MensajeRepository mensajeRepository;

    public List<Chat> buscarTodas() {
        return chatRepository.buscarTodos();
    }

    public Chat buscarPorId(Long id) {
        return chatRepository.buscarPorId(id).orElse(null);
    }

    public Chat crear(Chat chat) {
        chatRepository.crear(chat);
        return chatRepository
                .buscarTodos()
                .stream()
                .reduce((primero, segundo) -> segundo)
                .orElse(null);
    }

    public boolean actualizar(Long id, Chat chat) {
        return chatRepository.actualizar(id, chat) > 0;
    }

    public void abrirChat(Long id){
        int filas = chatRepository.cambiarEstado(id, "ABIERTO");
        if (filas == 0) {
            throw new RuntimeException("Chat no encontrado");
        }
    }
    public void cerrarChat(Long id){
        int filas = chatRepository.cambiarEstado(id, "CERRADO");
        if (filas == 0) {
            throw new RuntimeException("Chat no encontrado");
        }
    }

    public void bloquearChat(Long id){
        int filas = chatRepository.cambiarEstado(id, "BLOQUEADO");
        if (filas == 0) {
            throw new RuntimeException("Chat no encontrado");
        }
    }

    public List<Mensaje> buscarMensajes(Long idChat) {
        if (chatRepository.buscarPorId(idChat).isEmpty()) {
            throw new RuntimeException("El chat no existe");
        }
        return mensajeRepository.buscarPorChat(idChat);
    }
}
