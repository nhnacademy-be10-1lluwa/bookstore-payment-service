package com.nhnacademy.illuwa.domain.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PaymentStatus {
    // 대기 중, 승인 , 실패, 취소
    PENDING, APPROVED, FAILED, CANCELLED;

    @JsonCreator
    public static PaymentStatus forValue(String value) {
        for (PaymentStatus paymentStatus : PaymentStatus.values()) {
            if (paymentStatus.name().equalsIgnoreCase(value)) {
                return paymentStatus;
            }
        }
        return PaymentStatus.PENDING;
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

}

