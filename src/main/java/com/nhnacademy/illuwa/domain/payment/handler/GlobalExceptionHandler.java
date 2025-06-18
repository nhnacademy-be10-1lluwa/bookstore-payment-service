package com.nhnacademy.illuwa.domain.payment.handler;

import com.nhnacademy.illuwa.domain.payment.exception.OrderIdNotFoundException;
import com.nhnacademy.illuwa.domain.payment.exception.PaymentAlreadyCanceledException;
import com.nhnacademy.illuwa.domain.payment.exception.PaymentKeyNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderIdNotFoundException.class)
    public ResponseEntity<Object> handleOrderIdNotFoundException(OrderIdNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        body.put("code", "ORDER_ID_NOT_FOUND");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PaymentKeyNotFoundException.class)
    public ResponseEntity<Object> handlePaymentKeyNotFoundException(PaymentKeyNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        body.put("code", "PAYMENT_KEY_NOT_FOUND");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PaymentAlreadyCanceledException.class)
    public ResponseEntity<Object> handlePaymentAlreadyCanceledException(PaymentAlreadyCanceledException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("code", "PAYMENT_ALREADY_CANCELED");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


}
