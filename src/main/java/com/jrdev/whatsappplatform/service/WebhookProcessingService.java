package com.jrdev.whatsappplatform.service;

import tools.jackson.databind.JsonNode;
import com.jrdev.whatsappplatform.model.*;
import com.jrdev.whatsappplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class WebhookProcessingService {

    private final WhatsappInstanciaRepository instanciaRepo;
    private final ContactoRepository contactoRepo;
    private final ChatRepository chatRepo;
    private final MensajeRepository mensajeRepo;
    private final BotRouterService botRouterService;

    @Transactional
    public void procesarMensajeUpsert(String instanceName, JsonNode dataNode) {

        // 1. Extraer datos clave
        JsonNode keyNode = dataNode.path("key");
        String remoteJid = keyNode.path("remoteJid").asText();
        boolean fromMe = keyNode.path("fromMe").asBoolean();
        String messageId = keyNode.path("id").asText();

        // Manejo seguro del nombre del contacto
        String pushName = dataNode.has("pushName") ? dataNode.get("pushName").asText() : remoteJid;

        // Extraer el texto y el tipo real (conversation, imageMessage, etc.)
        JsonNode messageNode = dataNode.path("message");
        String tipoMensaje = dataNode.path("messageType").asText("conversation");
        String contenido = messageNode.has("conversation") ?
                messageNode.path("conversation").asText() :
                messageNode.path("extendedTextMessage").path("text").asText("[Mensaje multimedia]");

        // 2. Buscar Instancia
        WhatsappInstancia instancia = instanciaRepo.buscarPorInstanceName(instanceName)
                .orElseThrow(() -> new RuntimeException("Instancia no encontrada: " + instanceName));

        // 3. Buscar o Crear Contacto
        Contacto contacto = contactoRepo.buscarPorRemoteJid(remoteJid)
                .orElseGet(() -> {
                    Contacto nuevoContacto = new Contacto();
                    nuevoContacto.setRemotejid(remoteJid);
                    nuevoContacto.setPushName(pushName);
                    nuevoContacto.setNombre(pushName);
                    nuevoContacto.setIdEmpresa(instancia.getIdEmpresa());
                    nuevoContacto.setBloqueado(false); // IMPORTANTE para evitar errores SQL

                    Long nuevoId = contactoRepo.crear(nuevoContacto);
                    nuevoContacto.setIdContacto(nuevoId);
                    return nuevoContacto;
                });

        // 4. Buscar o Crear Chat
        Chat chat = chatRepo.buscarPorInstanciaYContacto(instancia.getIdWhatsappInstancia(), contacto.getIdContacto())
                .orElseGet(() -> {
                    Chat nuevoChat = new Chat();
                    nuevoChat.setIdWhatsAppInstancia(instancia.getIdWhatsappInstancia());
                    nuevoChat.setIdContacto(contacto.getIdContacto());
                    nuevoChat.setRemotejid(remoteJid);
                    nuevoChat.setTitulo(pushName);
                    nuevoChat.setEstado("ABIERTO");
                    nuevoChat.setUnread_count(0);
                    nuevoChat.setUltima_actividad(OffsetDateTime.now(ZoneOffset.UTC));

                    Long nuevoId = chatRepo.crear(nuevoChat);
                    nuevoChat.setIdChat(nuevoId);
                    return nuevoChat;
                });

        // 5. Crear y Guardar el Mensaje
        // 5. Crear y Guardar el Mensaje
        Mensaje mensaje = new Mensaje();
        mensaje.setIdChat(chat.getIdChat());
        mensaje.setEvolutionMessageId(messageId);
        mensaje.setContenido(contenido);
        mensaje.setTipo(tipoMensaje); // Ahora es dinámico
        mensaje.setSenderJid(remoteJid);
        mensaje.setEnviadoPorNosotros(fromMe);

        // CORRECCIÓN AQUÍ: Usar los valores exactos permitidos por el CHECK de PostgreSQL
        mensaje.setDireccion(fromMe ? "OUTGOING" : "INCOMING");

        mensaje.setEstado(dataNode.path("status").asText("RECEIVED"));
        mensaje.setFechaMensaje(OffsetDateTime.now(ZoneOffset.UTC));

        mensajeRepo.crear(mensaje);
        // 6. Actualizar el Chat
        chatRepo.actualizarUltimaActividad(chat.getIdChat());
        if (!fromMe) {
            chatRepo.incrementarUnreadCount(chat.getIdChat());
//            botRouterService.procesarRespuesta(instancia, contacto, mensaje);
        }
    }
}