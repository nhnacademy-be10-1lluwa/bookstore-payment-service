package com.nhnacademy.illuwa.domain.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Setter
    @Column(name = "payment_key", nullable = false)
    private String paymentKey;

    // 주문 번호
    @Setter
    @Column(name = "order_number", nullable = false)
    private String orderNumber;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus paymentStatus;

    // 총 금액
    @Setter
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Setter
    @Column(name = "approve_at", nullable = false)
    private LocalDateTime approveAt;

    @Setter
    @OneToOne
    @JoinColumn(name = "card_info_id", nullable = false)
    private CardInfoEntity cardInfoEntity;


}
