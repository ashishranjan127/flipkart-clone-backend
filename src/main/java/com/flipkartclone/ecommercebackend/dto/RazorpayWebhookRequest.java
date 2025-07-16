package com.flipkartclone.ecommercebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayWebhookRequest {

    private String entity;
    private String account_id;
    private String event;
    private String contains;
    private int created_at;
    private JsonNode payload;
}