package com.nhnacademy.illuwa.domain.payment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.entity.CardInfoEntity;
import com.nhnacademy.illuwa.domain.payment.entity.Payment;
import com.nhnacademy.illuwa.domain.payment.repository.CardInfoEntityRepository;
import com.nhnacademy.illuwa.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    private PaymentServiceImpl paymentService;

    private PaymentRepository paymentRepository;
    private CardInfoEntityRepository cardInfoEntityRepository;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        cardInfoEntityRepository = mock(CardInfoEntityRepository.class);
        restTemplate = mock(RestTemplate.class);
        objectMapper = mock(ObjectMapper.class);

        paymentService = new PaymentServiceImpl(
                paymentRepository,
                cardInfoEntityRepository,
                restTemplate,
                objectMapper
        );

        ReflectionTestUtils.setField(paymentService, "secretKey", "test_secret_key");
    }

    @Test
    void savePayment_shouldStoreToDB() {
        // given
        PaymentResponse.CardInfo card = new PaymentResponse.CardInfo();
        card.setIssuerCode("ISSUER");
        card.setAcquirerCode("ACQ");
        card.setNumber("1234-5678");
        card.setCardType("신용카드");
        card.setAmount(15000);

        PaymentResponse resp = new PaymentResponse();
        resp.setPaymentKey("payKey123");
        resp.setOrderId("ORDER123");
        resp.setStatus("DONE");
        resp.setTotalAmount(15000);
        resp.setApprovedAt(OffsetDateTime.now());
        resp.setCard(card);

        when(cardInfoEntityRepository.save(any())).thenReturn(new CardInfoEntity());
        when(paymentRepository.save(any())).thenReturn(new Payment());

        // when
        Payment result = paymentService.savePayment(resp);

        // then
        assertThat(result).isNotNull();
        verify(cardInfoEntityRepository).save(any(CardInfoEntity.class));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void findPaymentByOrderId_shouldReturnResponse() throws Exception {
        // given
        String orderId = "ORDER123";
        String jsonResponse = "{\"orderId\":\"ORDER123\"}";

        ResponseEntity<String> fakeResponse = new ResponseEntity<>(jsonResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class))
        ).thenReturn(fakeResponse);

        PaymentResponse mockResp = new PaymentResponse();
        mockResp.setOrderId(orderId);

        when(objectMapper.readValue(anyString(), eq(PaymentResponse.class))).thenReturn(mockResp);

        // when
        PaymentResponse result = paymentService.findPaymentByOrderId(orderId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo("ORDER123");

        verify(restTemplate).exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
        verify(objectMapper).readValue(anyString(), eq(PaymentResponse.class));
    }
}
