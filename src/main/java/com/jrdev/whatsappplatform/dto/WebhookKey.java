package com.jrdev.whatsappplatform.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookKey {

    private String id;

    private boolean fromMe;

    private String remoteJid;

    private String remoteJidAlt;

    private String participant;

    private String addressingMode;
}