package com.nhnacademy.illuwa.domain.payment.exception;

public class PaymentRefundFailedException extends RuntimeException {
    public PaymentRefundFailedException(String message) {
        super(message);
    }
}
