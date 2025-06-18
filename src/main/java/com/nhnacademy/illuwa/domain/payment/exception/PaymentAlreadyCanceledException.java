package com.nhnacademy.illuwa.domain.payment.exception;

public class PaymentAlreadyCanceledException extends RuntimeException {
    public PaymentAlreadyCanceledException(String message) {
        super(message);
    }
}
