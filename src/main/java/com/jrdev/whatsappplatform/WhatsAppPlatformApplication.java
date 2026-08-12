package com.jrdev.whatsappplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class WhatsAppPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(WhatsAppPlatformApplication.class, args);
    }
}