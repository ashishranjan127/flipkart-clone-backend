package com.flipkartclone.ecommercebackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderResponse {
    private String id; // Razorpay Order ID
    private String entity; // "order"
    private int amount; // Amount in smallest currency unit (e.g., paise for INR)
    private int amount_paid;
    private int amount_due;
    private String currency;
    private String receipt;
    private String status; // "created", "attempted", etc.
    private int attempts;
    private long created_at; // Unix timestamp
}