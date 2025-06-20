package com.nhnacademy.illuwa.domain.payment.handler;

import com.nhnacademy.illuwa.domain.payment.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest mockRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        mockRequest = mock(WebRequest.class);
        when(mockRequest.getDescription(false)).thenReturn("uri=/test");
    }

    @Test
    @DisplayName("OrderIdNotFoundException 처리")
    void handleOrderIdNotFoundException() {
        OrderIdNotFoundException ex = new OrderIdNotFoundException("주문 ID 없음");

        ResponseEntity<Object> response = handler.handleOrderIdNotFoundException(ex, mockRequest);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertThat(body.get("code")).isEqualTo("ORDER_ID_NOT_FOUND");
        assertThat(body.get("message")).isEqualTo("주문 ID 없음");
    }

    @Test
    @DisplayName("PaymentKeyNotFoundException 처리")
    void handlePaymentKeyNotFoundException() {
        PaymentKeyNotFoundException ex = new PaymentKeyNotFoundException("결제 키 없음");

        ResponseEntity<Object> response = handler.handlePaymentKeyNotFoundException(ex, mockRequest);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("code")).isEqualTo("PAYMENT_KEY_NOT_FOUND");
        assertThat(body.get("message")).isEqualTo("결제 키 없음");
    }

    @Test
    @DisplayName("PaymentAlreadyCanceledException 처리")
    void handlePaymentAlreadyCanceledException() {
        PaymentAlreadyCanceledException ex = new PaymentAlreadyCanceledException("이미 취소됨");

        ResponseEntity<Object> response = handler.handlePaymentAlreadyCanceledException(ex, mockRequest);
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(body.get("code")).isEqualTo("PAYMENT_ALREADY_CANCELED");
        assertThat(body.get("message")).isEqualTo("이미 취소됨");
        assertThat(body.get("path")).isEqualTo("/test");
    }

    @Test
    @DisplayName("InvalidPaymentStatusException 처리")
    void handleInvalidPaymentStatusException() {
        InvalidPaymentStatusException ex = new InvalidPaymentStatusException("상태 오류");

        ResponseEntity<Object> response = handler.handleInvalidPaymentStatusException(ex, mockRequest);
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(body.get("code")).isEqualTo("INVALID_STATUS");
        assertThat(body.get("message")).isEqualTo("상태 오류");
        assertThat(body.get("path")).isEqualTo("/test");
    }

    @Test
    @DisplayName("InvalidPaymentUriException 처리")
    void handleInvalidPaymentUriException() {
        InvalidPaymentUriException ex = new InvalidPaymentUriException("URI가 잘못됨");

        ResponseEntity<Object> response = handler.handleInvalidPaymentUriException(ex, mockRequest);
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(body.get("code")).isEqualTo("INVALID_URI");
        assertThat(body.get("message")).isEqualTo("URI가 잘못됨");
        assertThat(body.get("path")).isEqualTo("/test");
    }

    @Test
    @DisplayName("PaymentRefundFailedException 처리")
    void handlePaymentRefundFailedException() {
        PaymentRefundFailedException ex = new PaymentRefundFailedException("환불 실패");

        ResponseEntity<Object> response = handler.handlePaymentRefundFailedException(ex, mockRequest);
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(body.get("code")).isEqualTo("PAYMENT_REFUND_FAILED");
        assertThat(body.get("message")).isEqualTo("환불 실패");
        assertThat(body.get("path")).isEqualTo("/test");
    }
}
