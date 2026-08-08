package com.jrdev.pruebawhatsapp2;


import com.jrdev.pruebawhatsapp2.service.WhatsAppConsoleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class PruebaWhatsApp2Application implements CommandLineRunner {


    private final WhatsAppConsoleService consoleService;



    public PruebaWhatsApp2Application(
            WhatsAppConsoleService consoleService
    ){

        this.consoleService = consoleService;

    }



    public static void main(String[] args) {

        SpringApplication.run(
                PruebaWhatsApp2Application.class,
                args
        );

    }



    @Override
    public void run(String... args) {


        consoleService.menu();


    }


}