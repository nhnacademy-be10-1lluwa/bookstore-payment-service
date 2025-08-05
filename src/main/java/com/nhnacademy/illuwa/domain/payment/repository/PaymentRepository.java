package com.nhnacademy.illuwa.domain.payment.repository;

import com.nhnacademy.illuwa.domain.payment.entity.Payment;
import com.nhnacademy.illuwa.domain.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByPaymentKey(String paymentKey);
    Payment findByOrderNumber(String orderNumber); // 추가
    List<Payment> findByPaymentStatus(PaymentStatus status); // 상태별 조회용
}
