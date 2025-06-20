package com.nhnacademy.illuwa.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.dto.RefundRequest;
import com.nhnacademy.illuwa.domain.payment.service.PaymentService;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

    @Test
    @DisplayName("GET /v1/payments/orders/{orderId} - 조회 성공")
    void findPaymentByOrderId_success() throws Exception {
        PaymentResponse response = new PaymentResponse();
        response.setOrderId("order-123");

        when(paymentService.findPaymentByOrderId("order-123")).thenReturn(response);

        mockMvc.perform(get("/v1/payments/orders/order-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order-123"));
    }

    @Test
    @DisplayName("POST /v1/payments/{paymentKey}/cancel - 환불 성공")
    void cancelPayment_success() throws Exception {
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setCancelReason("테스트 환불");

        PaymentResponse response = new PaymentResponse();
        response.setPaymentKey("key");
        response.setStatus("CANCELLED");

        when(paymentService.cancelPayment(any())).thenReturn(response);

        String json = objectMapper.writeValueAsString(refundRequest);

        mockMvc.perform(post("/v1/payments/key/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentKey").value("key"))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("GET / - 결제 페이지")
    void index_success() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("/payment/checkout"));
    }

    @Test
    @DisplayName("GET /fail - 실패 페이지")
    void failPage_success() throws Exception {
        mockMvc.perform(get("/fail")
                        .param("code", "ERR001")
                        .param("message", "실패"))
                .andExpect(status().isOk())
                .andExpect(view().name("/fail"))
                .andExpect(model().attribute("code", "ERR001"))
                .andExpect(model().attribute("message", "실패"));
    }

    @Test
    @DisplayName("GET /callback-auth - 인증 응답")
    void callbackAuth_success() throws Exception {
        mockMvc.perform(get("/callback-auth")
                        .param("customerKey", "key123")
                        .param("code", "auth-code"))
                .andExpect(status().isOk()); // 실제 동작 확인은 통신 모킹 필요
    }

}
