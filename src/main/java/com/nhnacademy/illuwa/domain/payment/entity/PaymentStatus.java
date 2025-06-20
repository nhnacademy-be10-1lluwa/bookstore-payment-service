package com.nhnacademy.illuwa.domain.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nhnacademy.illuwa.domain.payment.exception.InvalidPaymentStatusException;

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
        throw new InvalidPaymentStatusException(value + "' 은 유효하지 않은 결제 상태입니다.");
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

}

