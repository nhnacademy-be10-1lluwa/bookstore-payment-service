package com.nhnacademy.illuwa.domain.payment.service.impl;

import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.dto.RefundRequest;
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

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
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
    @DisplayName("환불 요청 테스트 성공")
    void cancelPayment_success() {
        // 제공
        RefundRequest request = new RefundRequest();
        request.setPaymentKey("test-payment-key");
        request.setCancelReason("cancelReason");

        Payment mockPayment = new Payment();
        mockPayment.setPaymentKey("test-payment-key");
        mockPayment.setOrderNumber("order-id");
        mockPayment.setPaymentStatus(PaymentStatus.DONE);
        mockPayment.setTotalAmount(BigDecimal.valueOf(1000));
        mockPayment.setApproveAt(LocalDateTime.now());
        mockPayment.setCardInfoEntity(new CardInfoEntity());

        PaymentResponse.CardInfo cardInfo = new PaymentResponse.CardInfo();
        cardInfo.setIssuerCode("11");
        cardInfo.setAcquirerCode("22");
        cardInfo.setNumber("123456");
        cardInfo.setCardType("VISA");
        cardInfo.setAmount(1000);

        PaymentResponse response = new PaymentResponse();

        response.setPaymentKey("test-payment-key");
        response.setOrderId("order-id");
        response.setStatus("CANCELLED");
        response.setTotalAmount(1000);
        response.setApprovedAt(OffsetDateTime.now());
        response.setRequestedAt(OffsetDateTime.now());
        response.setCard(cardInfo);

        ResponseEntity<PaymentResponse> mockResponse = new ResponseEntity<>(response, HttpStatus.OK);

        // Mock 동작 정의
        when(paymentRepository.findByPaymentKey("test-payment-key")).thenReturn(mockPayment);
        when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(PaymentResponse.class)))
                .thenReturn(mockResponse);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PaymentResponse result = paymentServiceImpl.cancelPayment(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        assertThat(result.getPaymentKey()).isEqualTo("test-payment-key");

    }

    @Test
    @DisplayName("환불 요청 실패 - 존재하지 않는 PaymentKey")
    void cancelPayment_fail_notFound() {
        // Given
        RefundRequest request = new RefundRequest();
        request.setPaymentKey("invalid-key");
        request.setCancelReason("테스트");

        when(paymentRepository.findByPaymentKey("invalid-key")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> paymentServiceImpl.cancelPayment(request))
                .isInstanceOf(PaymentKeyNotFoundException.class)
                .hasMessageContaining("해당 결제 건을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("환불 요청 실패 - 이미 환불된 결제")
    void cancelPayment_fail_alreadyCancelled() {
        // Given
        RefundRequest request = new RefundRequest();
        request.setPaymentKey("cancelled-key");
        request.setCancelReason("이미 환불됨");

        Payment cancelledPayment = new Payment();
        cancelledPayment.setPaymentKey("cancelled-key");
        cancelledPayment.setOrderNumber("order-1");
        cancelledPayment.setPaymentStatus(PaymentStatus.CANCELLED);
        cancelledPayment.setTotalAmount(BigDecimal.valueOf(1000));
        cancelledPayment.setApproveAt(LocalDateTime.now());
        cancelledPayment.setCardInfoEntity(new com.nhnacademy.illuwa.domain.payment.entity.CardInfoEntity());

        when(paymentRepository.findByPaymentKey("cancelled-key")).thenReturn(cancelledPayment);

        // When & Then
        assertThatThrownBy(() -> paymentServiceImpl.cancelPayment(request))
                .isInstanceOf(PaymentAlreadyCanceledException.class)
                .hasMessageContaining("이미 환불 처리된 결제 정보입니다");
    }


    // -------------------------- 커버리지 채우기 ---------------------------- //

    @Test
    @DisplayName("cancelPayment - Toss 응답 body가 null")
    void cancelPayment_tossResponseBodyNull() {
        RefundRequest request = new RefundRequest();
        request.setPaymentKey("test-key");
        request.setCancelReason("이유");

        Payment mockPayment = new Payment();
        mockPayment.setPaymentKey("test-key");
        mockPayment.setPaymentStatus(PaymentStatus.DONE);
        mockPayment.setTotalAmount(BigDecimal.valueOf(1000));
        mockPayment.setApproveAt(LocalDateTime.now());
        mockPayment.setCardInfoEntity(new CardInfoEntity());

        when(paymentRepository.findByPaymentKey("test-key")).thenReturn(mockPayment);

        ResponseEntity<PaymentResponse> mockResponse = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(PaymentResponse.class)))
                .thenReturn(mockResponse);

        PaymentResponse result = paymentServiceImpl.cancelPayment(request);

        assertThat(result).isNull();
    }

    // ------------------------------------------------------------------- //

    @Test
    @DisplayName("findPaymentByOrderId - approvedAt만 null")
    void findPaymentByOrderId_approvedAtOnlyNull() {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentKey("test-key");
        response.setOrderId("order-id");
        response.setStatus("DONE");
        response.setTotalAmount(1000);
        response.setApprovedAt(null); // null
        response.setRequestedAt(OffsetDateTime.now()); // 값 있음
        response.setCard(new PaymentResponse.CardInfo());

        ResponseEntity<PaymentResponse> mockResponse = new ResponseEntity<>(response, HttpStatus.OK);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(PaymentResponse.class)))
                .thenReturn(mockResponse);

        PaymentResponse result = paymentServiceImpl.findPaymentByOrderId("order-id");

        assertThat(result).isNotNull();
        assertThat(result.getApprovedAt()).isNull();
        assertThat(result.getRequestedAt()).isNotNull();
    }

    @Test
    @DisplayName("cancelPayment - URI 생성 실패")
    void cancelPayment_uriSyntaxException() {
        // Given
        RefundRequest request = new RefundRequest();
        request.setPaymentKey("invalid|key");
        request.setCancelReason("사유");

        Payment mockPayment = new Payment();
        mockPayment.setPaymentKey("invalid|key");
        mockPayment.setPaymentStatus(PaymentStatus.DONE);
        mockPayment.setTotalAmount(BigDecimal.valueOf(1000));
        mockPayment.setApproveAt(LocalDateTime.now());
        mockPayment.setCardInfoEntity(new CardInfoEntity());

        when(paymentRepository.findByPaymentKey("invalid|key")).thenReturn(mockPayment);

        // When & Then
        assertThatThrownBy(() -> paymentServiceImpl.cancelPayment(request))
                .isInstanceOf(InvalidPaymentUriException.class)
                .hasMessageContaining("잘못된 URI 정보입니다.");
    }

    @Test
    @DisplayName("cancelPayment - Toss 응답이 실패인 경우 예외 발생")
    void cancelPayment_fail_dueToTossErrorResponse() {
        RefundRequest request = new RefundRequest();
        request.setPaymentKey("test-payment-key");
        request.setCancelReason("사유");

        Payment mockPayment = new Payment();
        mockPayment.setPaymentKey("test-payment-key");
        mockPayment.setPaymentStatus(PaymentStatus.DONE);
        mockPayment.setTotalAmount(BigDecimal.valueOf(1000));
        mockPayment.setApproveAt(LocalDateTime.now());
        mockPayment.setCardInfoEntity(new CardInfoEntity());

        when(paymentRepository.findByPaymentKey("test-payment-key")).thenReturn(mockPayment);

        ResponseEntity<PaymentResponse> mockResponse = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(PaymentResponse.class)))
                .thenReturn(mockResponse);

        assertThatThrownBy(() -> paymentServiceImpl.cancelPayment(request))
                .isInstanceOf(PaymentRefundFailedException.class)
                .hasMessageContaining("환불 요청에 실패했습니다.");
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

