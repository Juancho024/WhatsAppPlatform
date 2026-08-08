package com.jrdev.pruebawhatsapp2.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookKey {

    private String remoteJid;

    private boolean fromMe;

}