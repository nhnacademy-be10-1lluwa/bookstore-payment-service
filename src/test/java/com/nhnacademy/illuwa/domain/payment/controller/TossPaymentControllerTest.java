package com.nhnacademy.illuwa.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.entity.Payment;
import com.nhnacademy.illuwa.domain.payment.service.PaymentService;
import com.nhnacademy.illuwa.domain.payment.service.TossPaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TossPaymentControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private TossPaymentService tossPaymentService;

    @MockBean
    private PaymentService paymentService;

    @Test
    void fetchPaymentByOrderId_mock기반_정상응답() {
        // given
        String orderId = "ORDER123";
        PaymentResponse mockPayment = new PaymentResponse();
        mockPayment.setOrderId(orderId);

        when(tossPaymentService.findPaymentByOrderId(orderId))
                .thenReturn(mockPayment);

        // when
        ResponseEntity<PaymentResponse> response = restTemplate.getForEntity(
                "/v1/payments/orders/" + orderId,
                PaymentResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderId()).isEqualTo(orderId);
    }
}
