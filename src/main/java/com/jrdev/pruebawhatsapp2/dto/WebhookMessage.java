package com.jrdev.pruebawhatsapp2.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookMessage {

    private String event;

    private String instance;

    private WebhookData data;

    private String sender;

}