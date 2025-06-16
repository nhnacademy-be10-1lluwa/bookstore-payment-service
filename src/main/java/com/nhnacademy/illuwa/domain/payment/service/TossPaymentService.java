package com.nhnacademy.illuwa.domain.payment.service;

import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;

public interface TossPaymentService {
//    PaymentResponse confirmPayment(String paymentKey, String orderId, int amount);
    PaymentResponse fetchPaymentByOrderId(String orderId);
}
