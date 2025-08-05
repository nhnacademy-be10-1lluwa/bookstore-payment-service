package com.nhnacademy.illuwa.domain.payment.controller;

import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.entity.Payment;
import com.nhnacademy.illuwa.domain.payment.entity.PaymentStatus;
import com.nhnacademy.illuwa.domain.payment.repository.PaymentRepository;
import com.nhnacademy.illuwa.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStatusSyncScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @Scheduled(fixedDelay = 300000)
    public void syncPendingPayments() {
        List<Payment> pendingPayments = paymentRepository.findByPaymentStatus(PaymentStatus.READY);

        for (Payment payment : pendingPayments) {
            try {
                // 10분 이상 READY 상태인 결제만 체크
                if (payment.getApproveAt().isBefore(LocalDateTime.now().minusMinutes(10))) {
                    PaymentResponse actualStatus = paymentService.findPaymentByOrderId(payment.getOrderNumber());

                    // 상태가 다르면 동기화
                    if (!payment.getPaymentStatus().toString().equals(actualStatus.getStatus())) {
                        payment.setPaymentStatus(PaymentStatus.forValue(actualStatus.getStatus()));
                        paymentRepository.save(payment);
                    }
                }
            } catch (Exception e) {
                // 로그 기록 후 다음 결제 처리
                log.error("결제 상태 동기화 실패: ", payment.getOrderNumber());
            }
        }
    }
}
