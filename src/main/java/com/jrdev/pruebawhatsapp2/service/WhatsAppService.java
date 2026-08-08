package com.jrdev.pruebawhatsapp2.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class WhatsAppService {



    private final EvolutionClient evolutionClient;


    private final ChatMemoryService chatMemoryService;




    public void enviarMensaje(
            String numero,
            String mensaje
    ){


        evolutionClient.enviarMensaje(
                numero,
                mensaje
        );


        // guardar mensaje enviado
        chatMemoryService.guardarMensaje(
                numero,
                "YO: " + mensaje
        );


    }


}