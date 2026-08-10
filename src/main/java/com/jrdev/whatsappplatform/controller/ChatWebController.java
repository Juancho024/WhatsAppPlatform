package com.jrdev.whatsappplatform.controller;

import com.jrdev.whatsappplatform.model.Chat;
import com.jrdev.whatsappplatform.model.Contacto;
import com.jrdev.whatsappplatform.model.Mensaje;
import com.jrdev.whatsappplatform.model.WhatsappInstancia;
import com.jrdev.whatsappplatform.repository.ChatRepository;
import com.jrdev.whatsappplatform.repository.ContactoRepository;
import com.jrdev.whatsappplatform.repository.MensajeRepository;
import com.jrdev.whatsappplatform.repository.WhatsappInstanciaRepository;
import com.jrdev.whatsappplatform.service.EvolutionClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chats")
@CrossOrigin(originPatterns = "*") // Usar originPatterns en vez de origins
@RequiredArgsConstructor
public class ChatWebController {

    private final ChatRepository chatRepo;
    private final MensajeRepository mensajeRepo;
    private final ContactoRepository contactoRepo;
    private final WhatsappInstanciaRepository instanciaRepo;
    private final EvolutionClient evolutionClient;

    // ... (Tus otros métodos: listarChatsPorInstancia y listarMensajesDeChat) ...

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/enviar")
    public ResponseEntity<String> enviarMensajeWeb(@RequestBody EnviarMensajeRequest request) {
        try {
            // 1. Obtener la Instancia para sacar el idEmpresa
            WhatsappInstancia instancia = instanciaRepo.buscarPorInstanceName(request.getInstanceName()).orElse(null);
            if (instancia == null) {
                return ResponseEntity.badRequest().body("❌ Instancia no encontrada en la BD.");
            }

            String remoteJid = request.getNumeroDestino() + "@s.whatsapp.net";

            // 2. BUSCAR O CREAR CONTACTO
            Contacto contacto = contactoRepo.buscarPorRemoteJid(remoteJid).orElse(null);
            if (contacto == null) {
                contacto = new Contacto();
                contacto.setIdEmpresa(instancia.getIdEmpresa());
                contacto.setNumeroTelefono(request.getNumeroDestino());
                contacto.setRemotejid(remoteJid);
                contacto.setNombre(request.getNumeroDestino()); // Por defecto usamos el número como nombre
                contacto.setTipo("PERSONA");
                contacto.setBloqueado(false);

                Long idContacto = contactoRepo.crear(contacto);
                contacto.setIdContacto(idContacto);
            }

            // 3. BUSCAR O CREAR CHAT
            // Ojo: Asegúrate de usar el getter correcto para el ID de tu instancia (ej. getId() o getIdWhatsAppInstancia())
            Chat chat = chatRepo.buscarPorInstanciaYContacto(instancia.getIdWhatsappInstancia(), contacto.getIdContacto()).orElse(null);
            if (chat == null) {
                chat = new Chat();
                chat.setIdWhatsAppInstancia(instancia.getIdWhatsappInstancia());
                chat.setIdContacto(contacto.getIdContacto());
                chat.setRemotejid(remoteJid);
                chat.setEstado("ABIERTO");
                chat.setUnread_count(0);
                chat.setUltima_actividad(OffsetDateTime.now());

                Long idChat = chatRepo.crear(chat);
                chat.setIdChat(idChat);
            }

            // 4. GUARDAR EL MENSAJE COMO SALIENTE EN LA BD
            Mensaje mensaje = new Mensaje();
            mensaje.setIdChat(chat.getIdChat());
            mensaje.setContenido(request.getTextoMensaje());
            mensaje.setTipo("text");
            mensaje.setEnviadoPorNosotros(true);
            mensaje.setDireccion("OUTGOING");
            mensaje.setEstado("ENVIADO");
            mensaje.setFechaMensaje(OffsetDateTime.now());

            mensajeRepo.crear(mensaje);

            // 5. ACTUALIZAR ÚLTIMA ACTIVIDAD DEL CHAT PARA QUE SUBA EN LA LISTA
            chatRepo.actualizarUltimaActividad(chat.getIdChat());

            // 6. ENVIAR FÍSICAMENTE POR EVOLUTION API
            evolutionClient.enviarMensaje(
                    request.getInstanceName(),
                    request.getNumeroDestino(),
                    request.getTextoMensaje()
            );

            return ResponseEntity.ok("✅ Mensaje guardado en BD y enviado exitosamente a " + request.getNumeroDestino());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Error enviando mensaje: " + e.getMessage());
        }
    }

    // 1. Endpoint para traer los chats por el nombre de la instancia
    @GetMapping("/instancia/{nombreInstancia}")
    public ResponseEntity<List<Chat>> obtenerChatsPorInstancia(@PathVariable String nombreInstancia) {
        return instanciaRepo.buscarPorInstanceName(nombreInstancia)
                .map(instancia -> {
                    List<Chat> chats = chatRepo.buscarPorInstancia(instancia.getIdWhatsappInstancia());
                    return ResponseEntity.ok(chats);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 2. Endpoint para traer los mensajes de un chat específico
    @GetMapping("/web/{idChat}/mensajes")
    public ResponseEntity<List<Mensaje>> obtenerMensajesPorChat(@PathVariable Long idChat) {
        List<Mensaje> mensajes = mensajeRepo.buscarPorChat(idChat);
        return ResponseEntity.ok(mensajes);
    }

    @Data
    public static class EnviarMensajeRequest {
        private String instanceName;
        private String numeroDestino;
        private String textoMensaje;
    }
}