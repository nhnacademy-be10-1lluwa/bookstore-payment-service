package com.nhnacademy.illuwa.domain.payment.service;

import com.nhnacademy.illuwa.domain.payment.dto.PaymentConfirmRequest;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentRefundRequest;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;

public interface PaymentService {
    // 결제 승인
    PaymentResponse confirm(PaymentConfirmRequest request);
    // 저장
    void savePayment(PaymentResponse paymentResponse);
    //조회
    PaymentResponse findPaymentByOrderId(String orderId);
    //환불
    PaymentResponse cancelPayment(PaymentRefundRequest refundRequest);
}
