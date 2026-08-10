package com.jrdev.whatsappplatform.service;

import com.jrdev.whatsappplatform.model.Mensaje;
import com.jrdev.whatsappplatform.repository.ChatRepository;
import com.jrdev.whatsappplatform.repository.MensajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MensajeService {
    private final MensajeRepository mensajeRepository;
    private final ChatRepository chatRepository;

    public List<Mensaje> buscarTodas() {
        return mensajeRepository.buscarTodos();
    }

    public Mensaje buscarPorId(Long id) {
        return mensajeRepository.buscarPorId(id).orElse(null);
    }

    public Mensaje crear(Mensaje mensaje) {
        mensajeRepository.crear(mensaje);
        return mensajeRepository.buscarTodos().stream().reduce((primero, segundo) -> segundo).orElse(null);
    }

    public boolean actualizar(Long id, Mensaje mensaje) {
        return mensajeRepository.actualizar(id, mensaje) > 0;
    }

    public Mensaje crearEnChat(Long idChat, Mensaje mensaje) {
        // 1. Verificar que el chat exista
        if (chatRepository.buscarPorId(idChat).isEmpty()) {
            throw new RuntimeException("No existe el chat " + idChat);
        }
        // 2. Forzar el chat recibido por URL
        mensaje.setIdChat(idChat);
        // 3. Crear mensaje
        mensajeRepository.crear(mensaje);
        // 4. Actualizar última actividad del chat
        chatRepository.actualizarUltimaActividad(idChat);
        // 5. Si es mensaje entrante,
        // aumentar mensajes no leídos
        if ("INCOMING".equals(mensaje.getDireccion())) {
            chatRepository.incrementarUnreadCount(idChat);
        }
        return mensaje;
    }
}
