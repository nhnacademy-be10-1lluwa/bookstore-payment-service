package com.nhnacademy.illuwa.domain.payment.service;

import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.dto.RefundRequest;
import com.nhnacademy.illuwa.domain.payment.entity.Payment;

public interface PaymentService {
    // 저장
    Payment savePayment(PaymentResponse paymentResponse);
    //조회
    PaymentResponse findPaymentByOrderId(String orderId);
    //환불
    PaymentResponse cancelPayment(RefundRequest refundRequest);
}
