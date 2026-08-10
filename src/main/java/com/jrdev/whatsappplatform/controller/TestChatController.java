package com.jrdev.whatsappplatform.controller;

import com.jrdev.whatsappplatform.service.EvolutionClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-chat")
@RequiredArgsConstructor
public class TestChatController {

    private final EvolutionClient evolutionClient;

    @PostMapping("/enviar")
    public ResponseEntity<String> enviarMensajePrueba(@RequestBody MensajePruebaRequest request) {
        try {
            // Utilizamos el cliente de Evolution que ya construimos
            evolutionClient.enviarMensaje(
                    request.getInstanceName(),
                    request.getNumeroDestino(),
                    request.getTexto()
            );
            return ResponseEntity.ok("✅ Mensaje enviado a " + request.getNumeroDestino());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Error enviando mensaje: " + e.getMessage());
        }
    }

    // DTO interno solo para este controlador de pruebas
    @Data
    public static class MensajePruebaRequest {
        private String instanceName;
        private String numeroDestino; // Ej: "18091234567"
        private String texto;
    }
}