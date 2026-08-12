package com.jrdev.whatsappplatform.service;

import tools.jackson.databind.JsonNode;
import com.jrdev.whatsappplatform.model.*;
import com.jrdev.whatsappplatform.repository.WhatsappInstanciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebhookProcessingService {

    private final JdbcTemplate jdbcTemplate;
    private final WhatsappInstanciaRepository instanciaRepo;
    private final BotRouterService botRouterService;

    @Async // 🔥 ESTO LIBERA A EVOLUTION API DE INMEDIATO
    @Transactional
    public void procesarMensajeUpsert(String instanceName, JsonNode dataNode) {

        // 1. Extraer los datos del JSON
        JsonNode keyNode = dataNode.path("key");
        String remoteJid = keyNode.path("remoteJid").asText();
        boolean fromMe = keyNode.path("fromMe").asBoolean();
        String messageId = keyNode.path("id").asText();
        String pushName = dataNode.has("pushName") ? dataNode.get("pushName").asText() : remoteJid;

        JsonNode messageNode = dataNode.path("message");
        String tipoMensaje = dataNode.path("messageType").asText("conversation");
        String contenido = messageNode.has("conversation") ?
                messageNode.path("conversation").asText() :
                messageNode.path("extendedTextMessage").path("text").asText("[Mensaje multimedia]");

        // 2. Ejecutar la Función SQL (Los 4 pasos de BD en 1 solo viaje)
        String sql = "SELECT procesar_webhook_whatsapp(?, ?, ?, ?, ?, ?, ?)";

        // Usamos query con un RowCallbackHandler vacío para consumir el VOID de Postgres de forma segura
        jdbcTemplate.query(
                sql,
                rs -> {},
                instanceName, remoteJid, pushName, messageId, contenido, tipoMensaje, fromMe
        );

        // 3. Evaluar si hay que pasarle el mensaje al Bot
        if (!fromMe && contenido != null && !contenido.equals("[Mensaje multimedia]")) {

            // Buscamos la instancia para saber de qué empresa es
            WhatsappInstancia instancia = instanciaRepo.buscarPorInstanceName(instanceName)
                    .orElseThrow(() -> new RuntimeException("Instancia no encontrada: " + instanceName));

            // Armamos los objetos en memoria (RAM) para el router, sin consultar la BD
            Contacto contactoMemoria = new Contacto();
            contactoMemoria.setRemotejid(remoteJid);
            contactoMemoria.setNombre(pushName);

            Mensaje mensajeMemoria = new Mensaje();
            mensajeMemoria.setContenido(contenido);

            // ¡Arrancamos el motor!
            botRouterService.procesarRespuesta(instancia, contactoMemoria, mensajeMemoria);
        }
    }
}
