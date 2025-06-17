package com.nhnacademy.illuwa.domain.payment.controller;

import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.entity.Payment;
import com.nhnacademy.illuwa.domain.payment.service.PaymentService;
import com.nhnacademy.illuwa.domain.payment.service.TossPaymentService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payments")
public class TossPaymentController {

    private final TossPaymentService tossPaymentService;
    private final PaymentService paymentService;

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaymentResponse> fetchPaymentByOrderId(@PathVariable String orderId) {
        PaymentResponse resp = tossPaymentService.findPaymentByOrderId(orderId);
        return ResponseEntity.ok(resp);
    }
}
