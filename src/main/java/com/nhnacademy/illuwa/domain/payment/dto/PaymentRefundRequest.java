package com.nhnacademy.illuwa.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundRequest {
    private String orderNumber;
    private String cancelReason;
}
