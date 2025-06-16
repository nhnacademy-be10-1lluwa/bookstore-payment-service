package com.nhnacademy.illuwa.domain.payment.repository;

import com.nhnacademy.illuwa.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
