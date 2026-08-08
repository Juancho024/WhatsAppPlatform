package com.jrdev.pruebawhatsapp2.service;


import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class ChatMemoryService {


    private final Map<String,List<String>> chats = new HashMap<>();



    public void guardarMensaje(
            String numero,
            String mensaje
    ){

        chats
                .computeIfAbsent(
                        numero,
                        k -> new ArrayList<>()
                )
                .add(mensaje);


    }




    public Set<String> obtenerChats(){

        return chats.keySet();

    }





    public List<String> obtenerConversacion(
            String numero
    ){

        return chats.getOrDefault(
                numero,
                new ArrayList<>()
        );


    }



}