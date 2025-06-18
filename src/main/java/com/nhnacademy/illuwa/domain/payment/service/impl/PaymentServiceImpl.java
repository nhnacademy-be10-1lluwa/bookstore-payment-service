package com.nhnacademy.illuwa.domain.payment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.dto.RefundRequest;
import com.nhnacademy.illuwa.domain.payment.entity.CardInfoEntity;
import com.nhnacademy.illuwa.domain.payment.entity.Payment;
import com.nhnacademy.illuwa.domain.payment.entity.PaymentStatus;
import com.nhnacademy.illuwa.domain.payment.exception.OrderIdNotFoundException;
import com.nhnacademy.illuwa.domain.payment.exception.PaymentAlreadyCanceledException;
import com.nhnacademy.illuwa.domain.payment.exception.PaymentKeyNotFoundException;
import com.nhnacademy.illuwa.domain.payment.repository.CardInfoEntityRepository;
import com.nhnacademy.illuwa.domain.payment.repository.PaymentRepository;
import com.nhnacademy.illuwa.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final CardInfoEntityRepository cardInfoEntityRepository;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // application-dev.yml secret-key
    @Value("${toss.secret-key}")
    private String secretKey;

    // toss 의 결제 응답 dto -> PaymentResponse의 정보를 Payment에 저장
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
        payment.setPaymentKey(resp.getPaymentKey());
        payment.setOrderNumber(resp.getOrderId());
        payment.setPaymentStatus(PaymentStatus.forValue(resp.getStatus()));
        payment.setTotalAmount(BigDecimal.valueOf(resp.getTotalAmount()));
        payment.setApproveAt(
                resp.getApprovedAt().
                atZoneSameInstant(ZoneId.of("Asia/Seoul")).
                toLocalDateTime());
        payment.setCardInfoEntity(cardInfoEntity);

        return paymentRepository.save(payment);
    }

    // 조회
    @Override
    public PaymentResponse findPaymentByOrderId(String orderId) {
        try {
            // 결제 조회 url 구현
            URI uri = new URI("https://api.tosspayments.com/v1/payments/orders/" + orderId);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            // 요청을 보내고 받은 정보를 response에 저장함
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new OrderIdNotFoundException("해당 주문 번호를 찾지 못했습니다.");
            }

            PaymentResponse resp = objectMapper.readValue(response.getBody(), PaymentResponse.class);
            resp.setRequestedAt(
                    resp.getRequestedAt()
                            .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                            .toOffsetDateTime()
            );

            resp.setApprovedAt(
                    resp.getApprovedAt()
                            .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                            .toOffsetDateTime()
            );

            return resp;
        } catch (Exception e) {
            throw new RuntimeException("결제 정보 조회 중 오류 발생", e);
        }
    }

    // 환불
    @Override
    public PaymentResponse cancelPayment(RefundRequest refundRequest) {
        try {
            // 환불에 필요한 paymentKey
            String paymentKey = refundRequest.getPaymentKey();

            Payment payment = paymentRepository.findByPaymentKey(paymentKey);

            if (payment == null) {
                throw new PaymentKeyNotFoundException("해당 결제 건을 찾을 수 없습니다. \n" + "paymentKey: " + paymentKey);
            }
            if (payment.getPaymentStatus().equals(PaymentStatus.CANCELLED)) {
                throw new PaymentAlreadyCanceledException("이미 환불 처리된 결제 정보입니다. \n" + "paymentKey: " + paymentKey);
            }

            URI uri = new URI("https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel");

            // 환불 요청 사항
            Map<String, String> body = new HashMap<>();
            body.put("cancelReason", refundRequest.getCancelReason());

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, createHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(uri, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("환불 실패: " + response.getStatusCode());
            }

            payment.setPaymentStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);

            PaymentResponse resp = objectMapper.readValue(response.getBody(), PaymentResponse.class);
            resp.setRequestedAt(
                    resp.getRequestedAt()
                            .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                            .toOffsetDateTime()
            );

            resp.setApprovedAt(
                    resp.getApprovedAt()
                            .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                            .toOffsetDateTime()
            );

            return resp;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // 인증
    private HttpHeaders createHeaders() {
        // payment 개발자 센터의 인증 방식
        String credential = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));


        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + credential);
        // 요청 본문이 Json 형식이라는 걸 서버에 알림
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
