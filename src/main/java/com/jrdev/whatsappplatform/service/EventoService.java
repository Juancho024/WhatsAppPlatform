package com.jrdev.whatsappplatform.service;

import com.jrdev.whatsappplatform.model.Evento;
import com.jrdev.whatsappplatform.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventoService {
    private final EventoRepository eventoRepository;
    private final ObjectMapper objectMapper;

    public List<Evento> buscarTodas() {
        return eventoRepository.buscarTodos();
    }

    public Evento buscarPorId(Long id) {
        return eventoRepository.buscarPorId(id).orElse(null);
    }

    public Evento crear(Evento evento) {
        eventoRepository.crear(evento);
        return eventoRepository
                .buscarTodos()
                .stream()
                .reduce((primero, segundo) -> segundo)
                .orElse(null);
    }

    public boolean actualizar(Long id, Evento evento) {
        return eventoRepository.actualizar(id, evento) > 0;
    }

    public Evento recibirEvento(Long idWhatsAppInstancia, String tipoEvento, String externalEventId, String payload) {
        Map<String, Object> payloadMap =
                objectMapper.readValue(
                        payload,
                        new TypeReference<Map<String, Object>>() {}
                );

        Evento evento = new Evento();
        evento.setIdWhatsAppInstancia(idWhatsAppInstancia);
        evento.setTipoEvento(tipoEvento);
        evento.setExternal_event_id(externalEventId);
        evento.setPayload(payloadMap);
        evento.setEstado("RECIBIDO");
        evento.setReceivedAt(OffsetDateTime.now());
        eventoRepository.crear(evento);
        return evento;
    }

    public boolean marcarProcesado(Long id) {
        return eventoRepository.marcarProcesado(id) > 0;
    }

    public boolean marcarError(Long id, String error) {
        return eventoRepository.marcarError(id, error) > 0;
    }
}
