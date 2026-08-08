package com.jrdev.pruebawhatsapp2.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;



@Service
public class EvolutionClient {



    private final HttpClient client;



    @Value("${evolution.url}")
    private String url;


    @Value("${evolution.apikey}")
    private String apikey;


    @Value("${evolution.instance}")
    private String instance;




    public EvolutionClient(
            HttpClient client
    ){

        this.client = client;

    }





    public void enviarMensaje(
            String numero,
            String texto
    ){


        try{


            String endpoint =
                    url +
                            "/message/sendText/" +
                            instance;



            System.out.println("URL:");
            System.out.println(endpoint);



            String json =
                    """
                    {
                       "number":"%s",
                       "text":"%s"
                    }
                    """.formatted(
                            numero,
                            texto
                    );



            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(endpoint)
                            )
                            .header(
                                    "apikey",
                                    apikey
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(json)
                            )
                            .build();




            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );



            System.out.println(
                    "STATUS:"
                            + response.statusCode()
            );


            System.out.println(
                    response.body()
            );



        }catch(Exception e){

            e.printStackTrace();

        }


    }


}