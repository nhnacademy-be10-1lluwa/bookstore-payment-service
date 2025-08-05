package com.nhnacademy.illuwa.domain.payment.controller;

import com.nhnacademy.illuwa.client.OrderServiceClient;
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
    private final OrderServiceClient orderServiceClient;

    @Scheduled(fixedDelay = 300000)
    public void syncPendingPayments() {
        List<Payment> pendingPayments = paymentRepository.findByPaymentStatus(PaymentStatus.READY);

        for (Payment payment : pendingPayments) {
            try {
                // 10분 이상 READY 상태인 결제만 체크
                if (payment.getApproveAt().isBefore(LocalDateTime.now().minusMinutes(10))) {
                    PaymentResponse actualStatus = paymentService.findPaymentByOrderId(payment.getOrderNumber());

                    if("DONE".equals(actualStatus.getStatus())) {

                        payment.setPaymentStatus(PaymentStatus.DONE);
                        paymentRepository.save(payment);

                        // 주문 상태도 업데이트
                        orderServiceClient.updateOrderStatusToCompleted(payment.getOrderNumber());

                        log.info("결제 상태 동기화 완료: {}",  payment.getOrderNumber());
                    }
                }
            } catch (Exception e) {
                log.error("결제 상태 동기화 실패: ", payment.getOrderNumber(), e);
            }
        }
    }
}
