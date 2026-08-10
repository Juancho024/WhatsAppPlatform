package com.jrdev.whatsappplatform.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.jrdev.whatsappplatform.service.WebhookProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final ObjectMapper objectMapper;
    private final WebhookProcessingService webhookService;

    @PostMapping("/evolution")
    public ResponseEntity<String> recibirEvolution(@RequestBody String payload) {
        try {
            JsonNode json = objectMapper.readTree(payload);
            String tipoEvento = json.path("event").asText("UNKNOWN");
            String instance = json.path("instance").asText(null);

            System.out.println("========== WEBHOOK ==========");
            System.out.println("Evento: " + tipoEvento);
            System.out.println("Instancia: " + instance);
            System.out.println("=============================");

            // Enrutador de eventos
            if ("messages.upsert".equals(tipoEvento) && instance != null) {
                JsonNode dataNode = json.path("data");
                webhookService.procesarMensajeUpsert(instance, dataNode);
            }

            return ResponseEntity.ok("Webhook procesado");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error procesando payload: " + e.getMessage());
        }
    }
}