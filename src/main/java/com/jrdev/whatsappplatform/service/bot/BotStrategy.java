package com.jrdev.whatsappplatform.service.bot;

import com.jrdev.whatsappplatform.model.Contacto;
import com.jrdev.whatsappplatform.model.Integracion;
import com.jrdev.whatsappplatform.model.Mensaje;
import com.jrdev.whatsappplatform.model.WhatsappInstancia;

public interface BotStrategy {

    // Este método le dice al Router si este bot sabe manejar el tipo de integración
    boolean soporta(String tipoIntegracion);

    // Aquí va la lógica pesada de cada bot
    void procesarMensaje(WhatsappInstancia instancia, Contacto contacto, Mensaje mensaje, Integracion integracion);
}