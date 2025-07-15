package com.nhnacademy.illuwa.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentConfirmRequest;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.dto.RefundRequest;
import com.nhnacademy.illuwa.domain.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @RequestMapping("/confirm")
    public ResponseEntity<Void> confirmPayment(@RequestBody PaymentConfirmRequest request) throws Exception {
        paymentService.confirm(request);
        return ResponseEntity.ok().build();
    }

//      // @RestController일 경우
////    @GetMapping("/orders/{orderId}")
//    @GetMapping("/v1/payments/orders/{orderId}")
//    @ResponseBody
//    public ResponseEntity<PaymentResponse> findPaymentByOrderId(@PathVariable String orderId) {
//        PaymentResponse resp = paymentService.findPaymentByOrderId(orderId);
//        return ResponseEntity.ok(resp); // 반드시 @ResponseBody 필요
//    }
//
//    // 환불 (OrderId)
//    @PostMapping("/v1/payments/orders/{orderId}/cancel")
//    public ResponseEntity<PaymentResponse> cancelByOrderId(
//            @PathVariable String orderId,
//            @RequestBody @Valid RefundRequest refundRequest) {
//
//        PaymentResponse rep = paymentService.findPaymentByOrderId(orderId);
//
//        refundRequest.setPaymentKey(rep.getPaymentKey());
//
//        PaymentResponse response = paymentService.cancelPayment(refundRequest);
//
//        return ResponseEntity.ok(response);
//    }
}
