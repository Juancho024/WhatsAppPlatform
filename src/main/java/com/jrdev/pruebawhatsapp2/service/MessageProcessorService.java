package com.jrdev.pruebawhatsapp2.service;

import com.jrdev.pruebawhatsapp2.dto.WebhookMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MessageProcessorService {


    private final ChatMemoryService chatMemoryService;



    public void procesar(WebhookMessage webhook){

        System.out.println("ENTRÓ A MESSAGE PROCESSOR");
        if(webhook.getData() == null){
            return;
        }



        String numero = webhook.getSender().split("@")[0];



        String mensaje =
                webhook.getData()
                        .getMessage()
                        .getConversation();



        if(mensaje == null){

            mensaje = "[mensaje no texto]";

        }



        boolean esMio =
                webhook.getData()
                        .getKey()
                        .isFromMe();



        if(mensaje == null){
            return;
        }

        System.out.println();
        System.out.println("📩 NUEVO MENSAJE");
        System.out.println("Numero: "+numero);
        System.out.println("Mensaje: "+mensaje);
        System.out.println();

        if(esMio){

            chatMemoryService.guardarMensaje(
                    numero,
                    "YO: " + mensaje
            );


        }else{


            chatMemoryService.guardarMensaje(
                    numero,
                    "ELLOS: " + mensaje
            );



        }
        System.out.println(
                "GUARDANDO CHAT: "+numero
        );
        System.out.println(
                "GUARDANDO MENSAJE: "+mensaje
        );


    }


}