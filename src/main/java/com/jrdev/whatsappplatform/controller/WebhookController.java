package com.jrdev.whatsappplatform.controller;

import com.jrdev.whatsappplatform.repository.ChangeLogRepository;
import com.jrdev.whatsappplatform.repository.WhatsappInstanciaRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.jrdev.whatsappplatform.service.WebhookProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final ObjectMapper objectMapper;
    private final WebhookProcessingService webhookService;
    private final WhatsappInstanciaRepository instanciaRepository;
    private final ChangeLogRepository changelogRepository;

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

            // 2. Enrutador para ESTADO DE CONEXIÓN (El escaneo del QR / Desconexión)
            if ("connection.update".equals(tipoEvento) && instance != null) {
                String state = json.path("data").path("state").asText("");

                if ("open".equalsIgnoreCase(state)) {
                    // 🔥 El usuario escaneó el QR y se conectó con éxito
                    instanciaRepository.actualizarEstadoPorInstanceName(instance, "CONECTANDO");
                    System.out.println("✅ Instancia " + instance + " conectada con éxito y marcada como CONECTANDO.");
                } else if ("close".equalsIgnoreCase(state)) {
                    // 🔥 Se desconectó el dispositivo
                    instanciaRepository.actualizarEstadoPorInstanceName(instance, "DESCONECTADA");
                    System.out.println("⚠️ Instancia " + instance + " desconectada y marcada como DESCONECTADA.");
                }
            }

            return ResponseEntity.ok("Webhook procesado");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error procesando payload: " + e.getMessage());
        }
    }

    @PostMapping("/github")
    public ResponseEntity<String> recibirCommitGithub(@RequestBody Map<String, Object> payload) {
        try {
            // Verificamos que el JSON traiga la lista de commits
            if (payload.containsKey("commits")) {
                List<Map<String, Object>> commits = (List<Map<String, Object>>) payload.get("commits");

                for (Map<String, Object> commit : commits) {
                    String hash = (String) commit.get("id");
                    String mensaje = (String) commit.get("message");
                    String fecha = (String) commit.get("timestamp");

                    Map<String, String> authorMap = (Map<String, String>) commit.get("author");
                    String autor = authorMap.get("name");

                    // Guardamos en la base de datos
                    changelogRepository.guardarCommit(hash, mensaje, autor, fecha);
                    System.out.println("Nuevo commit guardado: " + mensaje);
                }
            }
            return ResponseEntity.ok("Webhook procesado correctamente");
        } catch (Exception e) {
            System.err.println("Error procesando webhook de GitHub: " + e.getMessage());
            return ResponseEntity.status(500).body("Error interno");
        }
    }
}