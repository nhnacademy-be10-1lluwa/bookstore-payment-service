package com.nhnacademy.illuwa.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentConfirmRequest;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentRefundRequest;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.service.PaymentService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private PaymentResponse createMockPaymentResponse(String orderId, String paymentKey, int totalAmount, String status, String orderName) {
        PaymentResponse.CardInfo cardInfo = new PaymentResponse.CardInfo();
        cardInfo.setIssuerCode("11");
        cardInfo.setAcquirerCode("22");
        cardInfo.setNumber("123456");
        cardInfo.setCardType("VISA");
        cardInfo.setAmount(totalAmount);

        PaymentResponse response = new PaymentResponse();
        response.setPaymentKey(paymentKey);
        response.setOrderId(orderId);
        response.setOrderName(orderName);
        response.setStatus(status);
        response.setTotalAmount(totalAmount);
        response.setApprovedAt(OffsetDateTime.now(ZoneOffset.UTC));
        response.setRequestedAt(OffsetDateTime.now(ZoneOffset.UTC));
        response.setCard(cardInfo);
        return response;
    }

    @Test
    @DisplayName("api/payments/confirm")
    void confirmPaymentTest() throws Exception {

        PaymentConfirmRequest request = new PaymentConfirmRequest("test-order-id", "test-payment-key", 1000);

        PaymentResponse expectedResponse = createMockPaymentResponse(
                request.getOrderNumber(),
                request.getPaymentKey(),
                request.getAmount(),
                "DONE",
                "test payment order"
        );
        when(paymentService.confirm(any(PaymentConfirmRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(request.getOrderNumber()))
                .andExpect(jsonPath("$.paymentKey").value(request.getPaymentKey()))
                .andExpect(jsonPath("$.totalAmount").value(request.getAmount()))
                .andExpect(jsonPath("$.status").value(expectedResponse.getStatus()));

    }

    @Test
    @DisplayName("/api/payments/refund")
    void refundPaymentTest() throws Exception {
        PaymentRefundRequest testRequest = new PaymentRefundRequest("test-order-id", "test");

        PaymentResponse testResponse = createMockPaymentResponse(
                testRequest.getOrderNumber(),
                "test-refund-payment-ket",
                1000,
                "CANCELED",
                "test payment refund"
        );
        when(paymentService.cancelPayment(any(PaymentRefundRequest.class))).thenReturn(testResponse);

        mockMvc.perform(post("/api/payments/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(testResponse.getOrderId()))
                .andExpect(jsonPath("$.paymentKey").value(testResponse.getPaymentKey()))
                .andExpect(jsonPath("$.totalAmount").value(testResponse.getTotalAmount()))
                .andExpect(jsonPath("$.status").value(testResponse.getStatus()))
                .andExpect(jsonPath("$.orderName").value(testResponse.getOrderName()));
    }



}

