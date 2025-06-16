package com.nhnacademy.illuwa.domain.payment.service;

import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.entity.Payment;

public interface PaymentService {
    Payment savePayment(PaymentResponse paymentResponse);

    Payment processPayment(String orderId);
}
