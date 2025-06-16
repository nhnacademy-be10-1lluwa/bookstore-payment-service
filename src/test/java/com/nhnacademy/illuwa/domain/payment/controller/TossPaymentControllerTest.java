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
        Payment mockPayment = new Payment();
        mockPayment.setOrderNumber(orderId);

        when(tossPaymentService.fetchPaymentByOrderId(orderId))
                .thenReturn(new PaymentResponse());

        when(paymentService.savePayment(any()))
                .thenReturn(mockPayment);

        // when
        ResponseEntity<Payment> response = restTemplate.getForEntity(
                "/v1/payments/orders/" + orderId,
                Payment.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderNumber()).isEqualTo(orderId);
    }
}
