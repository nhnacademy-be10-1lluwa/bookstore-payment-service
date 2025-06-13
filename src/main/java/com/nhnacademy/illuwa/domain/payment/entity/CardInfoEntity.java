package com.nhnacademy.illuwa.domain.payment.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "card_info")
public class CardInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Setter
    @Column(name = "issuer_code", length = 10, nullable = false)
    private String issuerCode;

    @Setter
    @Column(name = "accquirer_code", length = 10)
    private String acquirerCode;

    @Setter
    @Column(name = "card_number", length = 20, nullable = false)
    private String cardNumber;

    @Setter
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Setter
    @Column(name = "card_type", length = 30, nullable = false)
    private String cardType;


}
