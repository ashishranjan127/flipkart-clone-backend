package com.flipkartclone.ecommercebackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderRequest {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String receipt;
}