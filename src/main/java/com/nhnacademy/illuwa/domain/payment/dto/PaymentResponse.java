package com.nhnacademy.illuwa.domain.payment.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PaymentResponse {

    private String mId;
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String status;
    private OffsetDateTime requestedAt;
    private OffsetDateTime approvedAt;
    private CardInfo card;

    private int totalAmount;

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
