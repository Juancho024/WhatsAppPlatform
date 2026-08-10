package com.jrdev.whatsappplatform.service;

import com.jrdev.whatsappplatform.model.*;
import com.jrdev.whatsappplatform.repository.IntegracionRepository;
import com.jrdev.whatsappplatform.service.bot.BotStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BotRouterService {

    private final IntegracionRepository integracionRepo;
    private final List<BotStrategy> estrategias; // Spring inyecta todas las implementaciones aquí

    public void procesarRespuesta(WhatsappInstancia instancia, Contacto contacto, Mensaje mensaje) {

        // 1. Buscamos qué integración ACTIVA tiene la empresa dueña de esta instancia de WhatsApp
        Optional<Integracion> integracionOpt = integracionRepo.buscarActivaPorEmpresa(instancia.getIdEmpresa());

        if (integracionOpt.isEmpty()) {
            System.out.println("La empresa " + instancia.getIdEmpresa() + " no tiene integraciones activas. Ignorando mensaje.");
            return;
        }

        Integracion integracion = integracionOpt.get();

        // 2. Buscamos al especialista adecuado para este tipo de integración (ej: "CITAS", "VENTAS")
        BotStrategy estrategiaElegida = estrategias.stream()
                .filter(e -> e.soporta(integracion.getTipo()))
                .findFirst()
                .orElse(null);

        if (estrategiaElegida != null) {
            // ¡Que trabaje el especialista!
            estrategiaElegida.procesarMensaje(instancia, contacto, mensaje, integracion);
        } else {
            System.err.println("No hay un bot configurado para el tipo de integración: " + integracion.getTipo());
        }
    }
}