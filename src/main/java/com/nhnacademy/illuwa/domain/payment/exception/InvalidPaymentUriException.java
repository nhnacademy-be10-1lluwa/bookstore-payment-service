package com.nhnacademy.illuwa.domain.payment.exception;

public class InvalidPaymentUriException extends RuntimeException {
    public InvalidPaymentUriException(String message) {
        super(message);
    }
}
