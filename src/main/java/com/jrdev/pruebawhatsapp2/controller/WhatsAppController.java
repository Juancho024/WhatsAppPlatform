package com.jrdev.pruebawhatsapp2.controller;

import com.jrdev.pruebawhatsapp2.dto.SendMessageRequest;
import com.jrdev.pruebawhatsapp2.dto.SendMessageResponse;
import com.jrdev.pruebawhatsapp2.service.WhatsAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    public WhatsAppController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    @PostMapping("/send")
    public ResponseEntity<SendMessageResponse> enviarMensaje(
            @RequestBody SendMessageRequest request) {

        whatsAppService.enviarMensaje(
                request.getNumero(),
                request.getMensaje()
        );

        SendMessageResponse response = new SendMessageResponse();
        response.setSuccess(true);
        response.setMessage("Mensaje enviado correctamente.");
        response.setNumero(request.getNumero());

        return ResponseEntity.ok(response);
    }

}