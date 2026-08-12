package com.jrdev.whatsappplatform.service;

import org.springframework.stereotype.Service;

@Service
public class MenuHandler {

    private final SessionService sessionService;
    // Aquí luego inyectaremos el InventoryAdapter y el servicio para enviar mensajes

    public MenuHandler(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public void processMessage(String instanceName, String phoneNumber, String textMessage) {
        // 1. Buscamos en qué estado estaba el usuario
        String currentState = sessionService.getUserState(instanceName, phoneNumber);

        // 2. Evaluamos el estado y la respuesta
        switch (currentState) {
            case "NUEVO_CHAT":
                // Lógica para enviar mensaje de bienvenida
                System.out.println("Enviando menú principal a " + phoneNumber);
                // Lo pasamos al siguiente estado
                sessionService.saveUserState(instanceName, phoneNumber, "ESPERANDO_OPCION_PRINCIPAL");
                break;

            case "ESPERANDO_OPCION_PRINCIPAL":
                if (textMessage.equals("1")) {
                    System.out.println("El usuario quiere ver el catálogo");
                    sessionService.saveUserState(instanceName, phoneNumber, "ESPERANDO_CATEGORIA");
                } else if (textMessage.equals("2")) {
                    System.out.println("El usuario quiere agendar cita");
                    sessionService.saveUserState(instanceName, phoneNumber, "ESPERANDO_FECHA_CITA");
                } else {
                    System.out.println("Opción inválida. Reenviando menú.");
                }
                break;

            case "ESPERANDO_CATEGORIA":
                // Aquí en el futuro llamaremos a la API del cliente externo
                System.out.println("Buscando categoría " + textMessage + " en el inventario externo...");
                break;

            default:
                sessionService.clearSession(instanceName, phoneNumber);
                break;
        }
    }
}