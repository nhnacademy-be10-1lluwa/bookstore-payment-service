package com.nhnacademy.illuwa.domain.payment.service.impl;

import com.nhnacademy.illuwa.client.OrderServiceClient;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentConfirmRequest;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentRefundRequest;
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

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Disabled
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CardInfoEntityRepository cardInfoEntityRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private PaymentServiceImpl paymentServiceImpl;

    // 각 테스트 메서드 실행 전에 공통적으로 필요한 설정을 수행합니다.
    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(paymentServiceImpl, "secretKey", "test-secret-key");
    }

    private PaymentResponse createMockPaymentResponse(String orderId, String paymentKey, int totalAmount, String status, String orderName) {
        PaymentResponse.CardInfo cardInfo = new PaymentResponse.CardInfo();
        cardInfo.setIssuerCode("11");
        cardInfo.setAcquirerCode("22");
        cardInfo.setNumber("123456");
        cardInfo.setCardType("VISA");
        cardInfo.setAmount(totalAmount); // CardInfo.amount는 int

        PaymentResponse response = new PaymentResponse();
        response.setPaymentKey(paymentKey);
        response.setOrderId(orderId);
        response.setOrderName(orderName);
        response.setStatus(status);
        response.setTotalAmount(totalAmount); // PaymentResponse.totalAmount는 int
        response.setApprovedAt(OffsetDateTime.now(ZoneOffset.UTC)); // 시간대 의존성 줄이기 위해 UTC 사용
        response.setRequestedAt(OffsetDateTime.now(ZoneOffset.UTC));
        response.setCard(cardInfo);
        return response;
    }
    private Payment createMockPaymentEntity(String paymentKey, String orderNumber, BigDecimal totalAmount, PaymentStatus paymentStatus) {
        Payment payment = new Payment();
        payment.setPaymentKey(paymentKey);
        payment.setOrderNumber(orderNumber);
        payment.setTotalAmount(totalAmount); // Payment 엔티티의 totalAmount는 BigDecimal로 가정
        payment.setPaymentStatus(paymentStatus);
        payment.setApproveAt(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        return payment;
    }

    @Test
    @DisplayName("confirm: 결제 승인 성공 시 주문 상태 업데이트 및 DB 저장")
    void confirmTest_success() throws Exception {
        PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest(
                "order-id-confirm-test", "payment-key-confirm-test", 1500, "test-idempotency-key");
        String tossConfirmResponseJson = String.format(
                "{\"status\": \"DONE\", \"orderId\": \"%s\", \"paymentKey\": \"%s\", \"totalAmount\": %d}",
                confirmRequest.getOrderNumber(), confirmRequest.getPaymentKey(), confirmRequest.getAmount());
        ResponseEntity<String> tossConfirmResponseEntity = new ResponseEntity<>(tossConfirmResponseJson, HttpStatus.OK);
        when(restTemplate.postForEntity(
                eq("https://api.tosspayments.com/v1/payments/confirm"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(tossConfirmResponseEntity);

        PaymentResponse findPaymentResponse = createMockPaymentResponse(
                confirmRequest.getOrderNumber(),
                confirmRequest.getPaymentKey(),
                confirmRequest.getAmount(),
                "DONE",
                "테스트 주문 이름");
        ResponseEntity<PaymentResponse> findPaymentResponseEntity = new ResponseEntity<>(findPaymentResponse, HttpStatus.OK);
        when(restTemplate.exchange(
                // `argThat`을 사용하여 URI가 `confirmRequest.getOrderNumber()`를 포함하는지 정확히 검증합니다.
                argThat(uri -> uri.toString().contains(confirmRequest.getOrderNumber())),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenReturn(findPaymentResponseEntity);

        when(cardInfoEntityRepository.save(any(CardInfoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse result = paymentServiceImpl.confirm(confirmRequest);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(confirmRequest.getOrderNumber());
        assertThat(result.getPaymentKey()).isEqualTo(confirmRequest.getPaymentKey());
        assertThat(result.getTotalAmount()).isEqualTo(confirmRequest.getAmount()); // int 타입으로 직접 비교
        assertThat(result.getStatus()).isEqualTo("DONE");
        assertThat(result.getOrderName()).isEqualTo("테스트 주문 이름"); // `orderName`도 검증

        verify(restTemplate, times(1)).postForEntity(
                eq("https://api.tosspayments.com/v1/payments/confirm"),
                any(HttpEntity.class),
                eq(String.class)
        );
        verify(orderServiceClient, times(1)).updateOrderStatusToCompleted(confirmRequest.getOrderNumber());
        verify(restTemplate, times(1)).exchange(
                any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(PaymentResponse.class));
        verify(cardInfoEntityRepository, times(1)).save(any(CardInfoEntity.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("confirm: 결제 승인 실패 시 RuntimeException 발생")
    void confirm_failure_tossApiError() {
        PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest(
                "order-id-fail", "payment-key-fail", 500, "test-idempotency-key");

        ResponseEntity<String> tossErrorResponse = new ResponseEntity<>("Error from Toss", HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity(
                eq("https://api.tosspayments.com/v1/payments/confirm"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(tossErrorResponse);

        assertThatThrownBy(() -> paymentServiceImpl.confirm(confirmRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Toss 결제 승인 실패: Error from Toss");

        verify(restTemplate, times(1)).postForEntity(
                eq("https://api.tosspayments.com/v1/payments/confirm"),
                any(HttpEntity.class),
                eq(String.class)
        );

        verifyNoInteractions(orderServiceClient, cardInfoEntityRepository, paymentRepository);
    }

    @Test
    @DisplayName("savePayment: 결제 정보 저장 성공")
    void savePaymentTest() {
        PaymentResponse responseToSave = createMockPaymentResponse("order-id-save", "payment-key-save", 1500, "DONE", "저장 테스트 주문");

        when(cardInfoEntityRepository.save(any(CardInfoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        paymentServiceImpl.savePayment(responseToSave);

        verify(cardInfoEntityRepository, times(1) ).save(any(CardInfoEntity.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("findPaymentByOrderId: 주문 번호로 결제 정보 조회 성공")
    void findPaymentByOrderId_success() {
        // Given
        String orderId = "order-id-query";
        PaymentResponse expectedResponse = createMockPaymentResponse(orderId, "query-key", 2000, "DONE", "조회 성공용 주문");
        ResponseEntity<PaymentResponse> mockResponseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenReturn(mockResponseEntity);

        // When
        PaymentResponse result = paymentServiceImpl.findPaymentByOrderId(orderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getPaymentKey()).isEqualTo(expectedResponse.getPaymentKey());
        assertThat(result.getStatus()).isEqualTo(expectedResponse.getStatus());
        assertThat(result.getTotalAmount()).isEqualTo(expectedResponse.getTotalAmount());
        assertThat(result.getCard()).isNotNull();
        assertThat(result.getOrderName()).isEqualTo(expectedResponse.getOrderName());

        verify(restTemplate, times(1)).exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        );
    }

    @Test
    @DisplayName("findPaymentByOrderId: 주문 번호로 결제 정보 조회 실패 - 주문 번호를 찾지 못함 (404 Not Found)")
    void findPaymentByOrderId_failure() {
        String orderId = "non-existent-order-id";
        ResponseEntity<PaymentResponse> mockResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenReturn(mockResponse);

        assertThatThrownBy(() -> paymentServiceImpl.findPaymentByOrderId(orderId))
                .isInstanceOf(OrderIdNotFoundException.class)
                .hasMessageContaining("해당 주문 번호를 찾지 못했습니다.");

        verify(restTemplate, times(1)).exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        );
    }

    @Test
    @DisplayName("findPaymentByOrderId: 결제 조회 실패 - 잘못된 URI 정보")
    void findPaymentByOrderId_failure_invalidUri() {
        String invalidOrderId = "invalid uri path with space";

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(paymentServiceImpl, "findPaymentByOrderId", invalidOrderId))
                .isInstanceOf(InvalidPaymentUriException.class)
                .hasMessageContaining("잘못된 URI 정보입니다.");

        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("findPaymentByOrderId: Toss 응답 body가 null인 경우 null 반환")
    void findPaymentByOrderId_responseBodyNull() {
        String orderId = "order-id-null-body";
        ResponseEntity<PaymentResponse> mockResponse = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(PaymentResponse.class)))
                .thenReturn(mockResponse);

        PaymentResponse result = paymentServiceImpl.findPaymentByOrderId(orderId);

        assertThat(result).isNull();

        verify(restTemplate, times(1)).exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        );
    }


    // --- cancelPayment 메서드 테스트 ---

    @Test
    @DisplayName("cancelPayment: 결제 환불 성공 시 상태 업데이트 및 응답 반환")
    void cancelPayment_success() throws Exception {
        // Given
        PaymentRefundRequest refundRequest = new PaymentRefundRequest("order-id-cancel-ok", "고객 변심");
        String paymentKey = "test-payment-key-ok";

        Payment existingPayment = createMockPaymentEntity(paymentKey, refundRequest.getOrderNumber(), BigDecimal.valueOf(1000), PaymentStatus.DONE);

        // findPaymentByOrderId가 반환할 PaymentResponse (totalAmount int)
        PaymentResponse findPaymentResponse = createMockPaymentResponse(refundRequest.getOrderNumber(), paymentKey, 1000, "DONE", "환불 성공용 주문");
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenReturn(new ResponseEntity<>(findPaymentResponse, HttpStatus.OK));

        when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(existingPayment);

        // Toss 결제 취소 API가 반환할 PaymentResponse (totalAmount int)
        PaymentResponse cancelledTossResponse = createMockPaymentResponse(refundRequest.getOrderNumber(), paymentKey, 1000, "CANCELED", "취소된 주문");
        ResponseEntity<PaymentResponse> tossCancelResponseEntity = new ResponseEntity<>(cancelledTossResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(
                any(URI.class),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenReturn(tossCancelResponseEntity);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PaymentResponse result = paymentServiceImpl.cancelPayment(refundRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("CANCELED");
        assertThat(existingPayment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);

        verify(restTemplate, times(1)).exchange(
                any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(PaymentResponse.class));
        verify(paymentRepository, times(1)).findByPaymentKey(paymentKey);
        verify(restTemplate, times(1)).postForEntity(
                any(URI.class),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        );
        verify(paymentRepository, times(1)).save(existingPayment);
    }

    @Test
    @DisplayName("cancelPayment: 결제 환불 실패 - PaymentKey를 찾을 수 없음 (DB에 없음)")
    void cancelPayment_paymentKeyNotFound() {
        PaymentRefundRequest refundRequest = new PaymentRefundRequest("order-id-no-key", "고객 변심");
        String paymentKey = "test-payment-key-notfound";

        PaymentResponse findPaymentResponse = createMockPaymentResponse(refundRequest.getOrderNumber(), paymentKey, 1000, "DONE", "키 없음 테스트");
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenReturn(new ResponseEntity<>(findPaymentResponse, HttpStatus.OK));

        when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(null);

        assertThatThrownBy(() -> paymentServiceImpl.cancelPayment(refundRequest))
                .isInstanceOf(PaymentKeyNotFoundException.class)
                .hasMessageContaining("해당 결제 건을 찾을 수 없습니다.");

        verify(paymentRepository, times(1)).findByPaymentKey(paymentKey);
        verifyNoMoreInteractions(restTemplate);
        verifyNoInteractions(orderServiceClient);
    }

    @Test
    @DisplayName("cancelPayment: 결제 환불 실패 - 이미 환불된 결제 정보")
    void cancelPayment_alreadyCancelled() {
        PaymentRefundRequest refundRequest = new PaymentRefundRequest("order-id-already-cancelled", "고객 변심");
        String paymentKey = "test-payment-key-already";
        Payment existingPayment = createMockPaymentEntity(paymentKey, refundRequest.getOrderNumber(), BigDecimal.valueOf(1000), PaymentStatus.CANCELLED);

        PaymentResponse findPaymentResponse = createMockPaymentResponse(refundRequest.getOrderNumber(), paymentKey, 1000, "CANCELED", "이미 취소됨 테스트");
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenReturn(new ResponseEntity<>(findPaymentResponse, HttpStatus.OK));

        when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(existingPayment);

        assertThatThrownBy(() -> paymentServiceImpl.cancelPayment(refundRequest))
                .isInstanceOf(PaymentAlreadyCanceledException.class)
                .hasMessageContaining("이미 환불 처리된 결제 정보입니다.");

        verify(paymentRepository, times(1)).findByPaymentKey(paymentKey);
        verifyNoMoreInteractions(restTemplate);
        verifyNoInteractions(orderServiceClient);
    }

    @Test
    @DisplayName("cancelPayment: 환불 요청 실패 - Toss API 응답이 2xx가 아님")
    void cancelPayment_tossApiRefundFailed() {
        PaymentRefundRequest refundRequest = new PaymentRefundRequest("order-id-toss-fail", "고객 변심");
        String paymentKey = "test-payment-key-tossfail";
        Payment existingPayment = createMockPaymentEntity(paymentKey, refundRequest.getOrderNumber(), BigDecimal.valueOf(1000), PaymentStatus.DONE);

        PaymentResponse findPaymentResponse = createMockPaymentResponse(refundRequest.getOrderNumber(), paymentKey, 1000, "DONE", "Toss 실패 테스트");
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenReturn(new ResponseEntity<>(findPaymentResponse, HttpStatus.OK));

        when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(existingPayment);

        when(restTemplate.postForEntity(
                any(URI.class),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> paymentServiceImpl.cancelPayment(refundRequest))
                .isInstanceOf(PaymentRefundFailedException.class)
                .hasMessageContaining("환불 요청에 실패했습니다.");

        verify(restTemplate, times(1)).postForEntity(
                any(URI.class),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        );
        verify(paymentRepository, times(1)).findByPaymentKey(paymentKey);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("cancelPayment: 잘못된 URI 정보 예외 처리")
    void cancelPayment_invalidUri() {
        PaymentRefundRequest refundRequest = new PaymentRefundRequest("order-id-invalid-uri", "고객 변심");
        String invalidPaymentKey = "invalid uri path with space";

        Payment existingPayment = createMockPaymentEntity(invalidPaymentKey, refundRequest.getOrderNumber(), BigDecimal.valueOf(1000), PaymentStatus.DONE);

        PaymentResponse findPaymentResponse = createMockPaymentResponse(refundRequest.getOrderNumber(), invalidPaymentKey, 1000, "DONE", "잘못된 URI 테스트");
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PaymentResponse.class)
        )).thenReturn(new ResponseEntity<>(findPaymentResponse, HttpStatus.OK));

        when(paymentRepository.findByPaymentKey(invalidPaymentKey)).thenReturn(existingPayment);

        assertThatThrownBy(() -> paymentServiceImpl.cancelPayment(refundRequest))
                .isInstanceOf(InvalidPaymentUriException.class)
                .hasMessageContaining("잘못된 URI 정보입니다.");

        verify(restTemplate, never()).postForEntity(any(URI.class), any(HttpEntity.class), any(Class.class));
        verify(paymentRepository, times(1)).findByPaymentKey(invalidPaymentKey);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("PaymentStatus.forValue: 유효하지 않은 값 입력 시 InvalidPaymentStatusException 발생")
    void paymentStatus_forValue_invalidValue_throwsException() {
        String unmatchedStatus = "UNKNOWN_STATUS";

        assertThatThrownBy(() -> PaymentStatus.forValue(unmatchedStatus))
                .isInstanceOf(InvalidPaymentStatusException.class)
                .hasMessageContaining("유효하지 않은 결제 상태");
    }
}
