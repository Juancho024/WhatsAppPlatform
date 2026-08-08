package com.jrdev.pruebawhatsapp2.controller;


import com.jrdev.pruebawhatsapp2.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    private final WebhookService webhookService;

    @PostMapping
    public void recibirMensaje(@RequestBody String body){

        System.out.println("======================");
        System.out.println("MENSAJE RECIBIDO");
        System.out.println(body);
        System.out.println("======================");

        webhookService.procesarWebhook(body);

    }

}