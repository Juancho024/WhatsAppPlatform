package com.jrdev.whatsappplatform.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookData {

    private WebhookKey key;

    private WebhookText message;

}