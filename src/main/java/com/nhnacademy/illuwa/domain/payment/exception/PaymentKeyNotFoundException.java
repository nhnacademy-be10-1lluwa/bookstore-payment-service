package com.nhnacademy.illuwa.domain.payment.exception;

public class PaymentKeyNotFoundException extends RuntimeException {
    public PaymentKeyNotFoundException(String message) {
        super(message);
    }
}
