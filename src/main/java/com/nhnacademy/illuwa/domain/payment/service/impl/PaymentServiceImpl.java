package com.nhnacademy.illuwa.domain.payment.service.impl;

import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.entity.CardInfoEntity;
import com.nhnacademy.illuwa.domain.payment.entity.Payment;
import com.nhnacademy.illuwa.domain.payment.entity.PaymentStatus;
import com.nhnacademy.illuwa.domain.payment.repository.CardInfoEntityRepository;
import com.nhnacademy.illuwa.domain.payment.repository.PaymentRepository;
import com.nhnacademy.illuwa.domain.payment.service.PaymentService;
import com.nhnacademy.illuwa.domain.payment.service.TossPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final TossPaymentService tossPaymentService;
    private final CardInfoEntityRepository cardInfoEntityRepository;

    // toss 의 결제 응답 dtd -> PaymentResponse의 정보를 Payment에 저장
    @Override
    @Transactional
    public Payment savePayment(PaymentResponse resp) {
        CardInfoEntity card = new CardInfoEntity();
        card.setIssuerCode(resp.getCard().getIssuerCode());
        card.setAcquirerCode(resp.getCard().getAcquirerCode());
        card.setCardNumber(resp.getCard().getNumber());
        card.setCardType(resp.getCard().getCardType());
        card.setAmount(BigDecimal.valueOf(resp.getCard().getAmount()));

        CardInfoEntity cardInfoEntity = cardInfoEntityRepository.save(card);

        Payment payment = new Payment();
        payment.setCardInfoEntity(card);
        payment.setOrderNumber(resp.getOrderId());
        payment.setPaymentStatus(PaymentStatus.forValue(resp.getStatus()));
        payment.setTotalAmount(BigDecimal.valueOf(resp.getTotalAmount()));
        payment.setApproveAt(resp.getApprovedAt().toLocalDateTime());

        return paymentRepository.save(payment);
    }


    // orderId로 정보를 조호 후 db에 저장
    @Override
    @Transactional
    public Payment processPayment(String orderId) {

        PaymentResponse response = tossPaymentService.fetchPaymentByOrderId(orderId);

        return savePayment(response);
    }
}
