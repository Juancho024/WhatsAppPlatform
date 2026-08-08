package com.jrdev.pruebawhatsapp2.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jrdev.pruebawhatsapp2.dto.WebhookMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class WebhookService {


    private final MessageProcessorService messageProcessorService;


    private final ObjectMapper mapper = new ObjectMapper();



    public void procesarWebhook(String json){
        System.out.println("ENTRÓ A WEBHOOK SERVICE");

        try {


            WebhookMessage webhook =
                    mapper.readValue(json, WebhookMessage.class);



            messageProcessorService.procesar(webhook);



        }catch(Exception e){

            System.out.println("ERROR AL PROCESAR WEBHOOK");
            e.printStackTrace();

        }


    }



}