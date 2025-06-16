package com.nhnacademy.illuwa.domain.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PaymentStatus {
    // payment 개발자 센터의 status 사용
    // 대기 중, 승인 , 실패, 취소
    READY, DONE, ABORTED, CANCELLED;

    @JsonCreator
    public static PaymentStatus forValue(String value) {
        for (PaymentStatus paymentStatus : PaymentStatus.values()) {
            if (paymentStatus.name().equalsIgnoreCase(value)) {
                return paymentStatus;
            }
        }
        return PaymentStatus.READY;
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

}

