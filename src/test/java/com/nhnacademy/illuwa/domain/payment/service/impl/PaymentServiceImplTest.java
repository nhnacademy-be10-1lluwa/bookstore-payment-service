package com.nhnacademy.illuwa.domain.payment.service.impl;

import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.entity.CardInfoEntity;
import com.nhnacademy.illuwa.domain.payment.entity.Payment;
import com.nhnacademy.illuwa.domain.payment.entity.PaymentStatus;
import com.nhnacademy.illuwa.domain.payment.repository.CardInfoEntityRepository;
import com.nhnacademy.illuwa.domain.payment.repository.PaymentRepository;
import com.nhnacademy.illuwa.domain.payment.service.TossPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CardInfoEntityRepository cardInfoEntityRepository;

    @Mock
    private TossPaymentService tossPaymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessPayment() {
        // given
        String orderId = "ORDER123";

        // Mock PaymentResponse
        PaymentResponse.CardInfo cardInfo = new PaymentResponse.CardInfo();
        cardInfo.setAcquirerCode("acq");
        cardInfo.setIssuerCode("iss");
        cardInfo.setCardType("신용");
        cardInfo.setNumber("1234-5678-9012-3456");
        cardInfo.setAmount(10000);

        PaymentResponse mockResponse = new PaymentResponse();
        mockResponse.setCard(cardInfo);
        mockResponse.setOrderId(orderId);
        mockResponse.setStatus("SUCCESS");
        mockResponse.setTotalAmount(10000);
        mockResponse.setApprovedAt(OffsetDateTime.now());

        Payment savedPayment = new Payment();  // 더미 리턴값

        // stubbing
        when(tossPaymentService.fetchPaymentByOrderId(orderId)).thenReturn(mockResponse);
        when(cardInfoEntityRepository.save(any())).thenReturn(new CardInfoEntity());
        when(paymentRepository.save(any())).thenReturn(savedPayment);

        // when
        Payment result = paymentService.processPayment(orderId);

        // then
        verify(tossPaymentService).fetchPaymentByOrderId(orderId);
        verify(cardInfoEntityRepository).save(any(CardInfoEntity.class));
        verify(paymentRepository).save(any(Payment.class));

        assertThat(result).isEqualTo(savedPayment);
    }

}
