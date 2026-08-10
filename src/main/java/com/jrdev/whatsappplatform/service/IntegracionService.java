package com.jrdev.whatsappplatform.service;

import com.jrdev.whatsappplatform.model.Integracion;
import com.jrdev.whatsappplatform.repository.IntegracionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IntegracionService {
    private final IntegracionRepository integracionRepository;

    public List<Integracion> buscarTodas() {
        return integracionRepository.buscarTodos();
    }

    public Integracion buscarPorId(Long id) {
        return integracionRepository.buscarById(id).orElse(null);
    }

    public Integracion crear(Integracion integracion) {
        integracionRepository.crear(integracion);
        return integracionRepository
                .buscarTodos()
                .stream()
                .reduce((primero, segundo) -> segundo)
                .orElse(null);
    }

    public boolean actualizar(Long id, Integracion integracion) {
        return integracionRepository.actualizar(id, integracion) > 0;
    }
}
