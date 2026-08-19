package com.jrdev.whatsappplatform.service;

import com.jrdev.whatsappplatform.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public void logAccion(Long idEmpresa, Long idUsuario, String accion, String tabla, Long registroId, String notas) {
        // Formateamos las notas a un JSON simple
        String jsonDetalles = String.format("{\"notas\": \"%s\"}", notas.replace("\"", "\\\""));

        auditoriaRepository.registrarAccion(idEmpresa, idUsuario, accion, tabla, registroId, jsonDetalles);
    }
}