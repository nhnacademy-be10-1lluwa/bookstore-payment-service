package com.nhnacademy.illuwa.domain.payment.exception;

public class OrderIdNotFoundException extends RuntimeException {
    public OrderIdNotFoundException(String message) {
        super(message);
    }
}
