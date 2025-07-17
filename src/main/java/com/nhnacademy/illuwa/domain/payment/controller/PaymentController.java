package com.nhnacademy.illuwa.domain.payment.controller;

import com.nhnacademy.illuwa.domain.payment.dto.PaymentConfirmRequest;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentRefundRequest;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @RequestMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@RequestBody PaymentConfirmRequest request) throws Exception {
        PaymentResponse paymentResponse = paymentService.confirm(request);
        return ResponseEntity.ok(paymentResponse);
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@RequestBody PaymentRefundRequest refundRequest) {
        PaymentResponse paymentResponse = paymentService.cancelPayment(refundRequest);
        return ResponseEntity.ok(paymentResponse);
    }
}
