package com.nhnacademy.illuwa.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentConfirmRequest {
    private String orderNumber;
    private String paymentKey;
    private int amount;
}