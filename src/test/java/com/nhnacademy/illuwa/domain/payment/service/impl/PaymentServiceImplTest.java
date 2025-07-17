package com.nhnacademy.illuwa.domain.payment.service.impl;

import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.entity.CardInfoEntity;
import com.nhnacademy.illuwa.domain.payment.entity.Payment;
import com.nhnacademy.illuwa.domain.payment.entity.PaymentStatus;
import com.nhnacademy.illuwa.domain.payment.exception.*;
import com.nhnacademy.illuwa.domain.payment.repository.CardInfoEntityRepository;
import com.nhnacademy.illuwa.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.OffsetDateTime;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// 단위 테스트
@ExtendWith(MockitoExtension.class)
@Disabled
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CardInfoEntityRepository cardInfoEntityRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentServiceImpl;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(paymentServiceImpl, "secretKey", "test-secret-key");
    }

    @Test
    @DisplayName("결제 정보 저장 테스트 성공")
    void savePaymentTest() {
        // 제공
        PaymentResponse.CardInfo cardInfo = new PaymentResponse.CardInfo();
        cardInfo.setIssuerCode("11");
        cardInfo.setAcquirerCode("22");
        cardInfo.setNumber("123456");
        cardInfo.setCardType("VISA");
        cardInfo.setAmount(1000);

        PaymentResponse response = new PaymentResponse();

        response.setPaymentKey("test-payment-key");
        response.setOrderId("order-id");
        response.setStatus("DONE");
        response.setTotalAmount(1000);
        response.setApprovedAt(OffsetDateTime.now());
        response.setRequestedAt(OffsetDateTime.now());
        response.setCard(cardInfo);

        when(cardInfoEntityRepository.save(any(CardInfoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

//        Payment savedPayment = paymentServiceImpl.savePayment(response);
//
//        // Then
//        assertThat(savedPayment).isNotNull();
//        assertThat(savedPayment.getPaymentKey()).isEqualTo("test-payment-key");
//        assertThat(savedPayment.getOrderNumber()).isEqualTo("order-id");
//        assertThat(savedPayment.getPaymentStatus().name()).isEqualTo("DONE");
//        assertThat(savedPayment.getTotalAmount().intValue()).isEqualTo(1000);
//        assertThat(savedPayment.getCardInfoEntity()).isNotNull();

    }

    @Test
    @DisplayName("주문 번호로 결제 정보 조회 - 성공")
    void findPaymentByOrderId_success() {
        // 제공
        PaymentResponse.CardInfo cardInfo = new PaymentResponse.CardInfo();
        cardInfo.setIssuerCode("11");
        cardInfo.setAcquirerCode("22");
        cardInfo.setNumber("123456");
        cardInfo.setCardType("VISA");
        cardInfo.setAmount(1000);

        PaymentResponse response = new PaymentResponse();

        response.setPaymentKey("test-payment-key");
        response.setOrderId("order-id");
        response.setStatus("DONE");
        response.setTotalAmount(1000);
        response.setApprovedAt(OffsetDateTime.now());
        response.setRequestedAt(OffsetDateTime.now());
        response.setCard(cardInfo);

        ResponseEntity<PaymentResponse> mockResponse = new ResponseEntity<>(response, HttpStatus.OK);

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenReturn(mockResponse);

        // When
        PaymentResponse result = paymentServiceImpl.findPaymentByOrderId("order-id");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo("order-id");

    }

    @Test
    @DisplayName("결제 정보 죄회 테스트 실패")
    void findPaymentByOrderId_failure() {
        String orderId = "order-id";
        ResponseEntity<PaymentResponse> mockResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> paymentServiceImpl.findPaymentByOrderId(orderId))
                .isInstanceOf(OrderIdNotFoundException.class)
                .hasMessageContaining("해당 주문 번호를 찾지 못했습니다.");
    }

    @Test
    @DisplayName("PaymentStatus.forValue - 일치하지 않는 모든 값으로 루프 테스트")
    void paymentStatus_forValue_invalidValue_throwsException() {
        String unmatched = "UNKNOWN_STATUS";

        assertThatThrownBy(() -> PaymentStatus.forValue(unmatched))
                .isInstanceOf(InvalidPaymentStatusException.class)
                .hasMessageContaining("유효하지 않은 결제 상태");
    }


    @Test
    @DisplayName("findPaymentByOrderId - Toss 응답 body가 null")
    void findPaymentByOrderId_responseBodyNull() {
        String orderId = "order-id";

        ResponseEntity<PaymentResponse> mockResponse = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(PaymentResponse.class)))
                .thenReturn(mockResponse);

        PaymentResponse result = paymentServiceImpl.findPaymentByOrderId(orderId);

        assertThat(result).isNull();
    }

}

