package com.nhnacademy.illuwa.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PaymentResponse {

    private String mId;
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String status;
    // 요청시간
    private OffsetDateTime requestedAt;
    // 승인사간
    private OffsetDateTime approvedAt;
    private int totalAmount;

    private CardInfo card;

    @Getter
    @NoArgsConstructor
    @Setter
    public static class CardInfo {
        private String issuerCode;
        private String acquirerCode;
        private String number;
        private String cardType;
        private int amount;
    }

}
