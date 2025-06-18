package com.nhnacademy.illuwa.domain.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RefundRequest {
    // 전체 환불의 경우 필요한 정보
    private String paymentKey;
    private String cancelReason;
    // 부분 환불일 경우 카드와 환불할 금액 필요
}
