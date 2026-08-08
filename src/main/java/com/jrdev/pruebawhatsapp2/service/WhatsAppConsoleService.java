package com.jrdev.pruebawhatsapp2.service;


import org.springframework.stereotype.Service;

import java.util.Scanner;



@Service
public class WhatsAppConsoleService {


    private final ChatMemoryService chatMemoryService;


    private final WhatsAppService whatsapp;



    private Scanner scanner =
            new Scanner(System.in);



    public WhatsAppConsoleService(
            ChatMemoryService memory,
            WhatsAppService whatsapp
    ){

        this.chatMemoryService=memory;
        this.whatsapp=whatsapp;

    }



    public void menu(){


        while(true){


            System.out.println("""
 
====== WHATSAPP ======

1 - Enviar mensaje

2 - Ver chats

3 - Ver conversación

4 - Salir

=======================

Seleccione una opcion:  """);



            String op =
                    scanner.nextLine();



            switch(op){


                case "1":

                    System.out.print("Numero: ");
                    String numero2 = scanner.nextLine();


                    System.out.print("Mensaje: ");
                    String msg=scanner.nextLine();


                    whatsapp.enviarMensaje(
                            numero2,
                            msg
                    );


                    break;



                case "2":


                    System.out.println("\nCHATS");


                    for(String numero :
                            chatMemoryService.obtenerChats()){


                        System.out.println(
                                "- " + numero
                        );

                    }

                    break;

                case "3":


                    System.out.print(
                            "Numero del chat:"
                    );


                    String numeroChat =
                            scanner.nextLine();



                    System.out.println(
                            "\nCONVERSACION"
                    );


                    for(String mensaje :
                            chatMemoryService.obtenerConversacion(numeroChat)){


                        System.out.println(
                                mensaje
                        );


                    }

                    break;



                case "4":

                    return;



            }



        }



    }


}