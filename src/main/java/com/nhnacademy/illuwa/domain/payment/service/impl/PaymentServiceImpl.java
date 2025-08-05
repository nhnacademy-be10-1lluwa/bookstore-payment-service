package com.nhnacademy.illuwa.domain.payment.service.impl;

import com.nhnacademy.illuwa.client.OrderServiceClient;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentConfirmRequest;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentRefundRequest;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.entity.CardInfoEntity;
import com.nhnacademy.illuwa.domain.payment.entity.Payment;
import com.nhnacademy.illuwa.domain.payment.entity.PaymentStatus;
import com.nhnacademy.illuwa.domain.payment.exception.*;
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
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
class PaymentServiceImpl implements PaymentService {

    private final OrderServiceClient orderServiceClient;
    private final RestTemplate restTemplate;

    private final PaymentRepository paymentRepository;
    private final CardInfoEntityRepository cardInfoEntityRepository;

    @Value("${toss.secret-key}")
    private String secretKey;

    @Override
    @Transactional
    public PaymentResponse confirm(PaymentConfirmRequest request) {
        // 1. 이미 처리된 결제인지 확인 (멱등성 보장)
        Payment existingPayment = paymentRepository.findByOrderNumber(request.getOrderNumber());
        if (existingPayment != null && existingPayment.getPaymentStatus() == PaymentStatus.DONE) {
            return findPaymentByOrderId(request.getOrderNumber());
        }

        try {
            // 2. Toss API 호출
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(secretKey, "");
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = Map.of(
                    "paymentKey", request.getPaymentKey(),
                    "orderId", request.getOrderNumber(),
                    "amount", request.getAmount()
            );

            HttpEntity<?> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.tosspayments.com/v1/payments/confirm",
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Toss 결제 승인 실패: " + response.getBody());
            }

            // 3. 결제 정보 저장
            PaymentResponse paymentResponse = findPaymentByOrderId(request.getOrderNumber());
            savePayment(paymentResponse);

            // 4. 주문 상태 업데이트
            orderServiceClient.updateOrderStatusToCompleted(request.getOrderNumber());

            return paymentResponse;

        } catch (Exception e) {
            // 오류 발생 시 결제 상태 검증 및 복구
            return handlePaymentError(request.getOrderNumber(), e);
        }
    }

    // toss 의 결제 응답 dto -> PaymentResponse의 정보를 Payment에 저장
    @Override
    @Transactional
    public void savePayment(PaymentResponse resp) {
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
        paymentRepository.save(payment);
    }

    // 조회
    @Override
    public PaymentResponse findPaymentByOrderId(String orderId) {
        // 결제 조회 url 구현
        URI uri = null;
        try {
            uri = new URI("https://api.tosspayments.com/v1/payments/orders/" + orderId);
        } catch (URISyntaxException e) {
            throw new InvalidPaymentUriException("잘못된 URI 정보입니다.");
        }

        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
        // 요청을 보내고 받은 정보를 response에 저장함
        ResponseEntity<PaymentResponse> response = restTemplate.exchange(uri, HttpMethod.GET, entity, PaymentResponse.class);

        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new OrderIdNotFoundException("해당 주문 번호를 찾지 못했습니다.");
        }

        PaymentResponse resp = response.getBody();

        if (resp != null) {
            resp.setRequestedAt(toKST(resp.getRequestedAt()));

            if (resp.getApprovedAt() != null) {
                resp.setApprovedAt(toKST(resp.getApprovedAt()));
            }
        }

        return resp;

    }

    // 환불
    @Override
    public PaymentResponse cancelPayment(PaymentRefundRequest refundRequest) {
        PaymentResponse paymentResponse = findPaymentByOrderId(refundRequest.getOrderNumber());

        // 환불에 필요한 paymentKey
        String paymentKey = paymentResponse.getPaymentKey();

        Payment payment = paymentRepository.findByPaymentKey(paymentKey);

        if (payment == null) {
            throw new PaymentKeyNotFoundException("해당 결제 건을 찾을 수 없습니다. \n" + "paymentKey: " + paymentKey);
        }
        if (payment.getPaymentStatus().equals(PaymentStatus.CANCELLED)) {
            throw new PaymentAlreadyCanceledException("이미 환불 처리된 결제 정보입니다. \n" + "paymentKey: " + paymentKey);
        }

        URI uri = null;
        try {
            uri = new URI("https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel");
        } catch (URISyntaxException e) {
            throw new InvalidPaymentUriException("잘못된 URI 정보입니다.");
        }

        // 환불 요청 사항
        Map<String, String> body = new HashMap<>();
        body.put("cancelReason", refundRequest.getCancelReason());

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, createHeaders());
        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(uri, entity, PaymentResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new PaymentRefundFailedException("환불 요청에 실패했습니다.");
        }

        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        PaymentResponse resp = response.getBody();

        if (resp != null) {
            resp.setRequestedAt(toKST(resp.getRequestedAt()));

            if (resp.getApprovedAt() != null) {
                resp.setApprovedAt(toKST(resp.getApprovedAt()));
            }
        }

        return resp;
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


    private OffsetDateTime toKST(OffsetDateTime dateTime) {
        return dateTime.atZoneSameInstant(ZoneId.of("Asia/Seoul")).toOffsetDateTime();
    }

    // 결제 오류 처리 및 상태 동기화
    private PaymentResponse handlePaymentError(String orderNumber, Exception originalException) {
        try {
            // Toss API로 실제 결제 상태 조회
            PaymentResponse actualStatus = findPaymentByOrderId(orderNumber);

            if ("DONE".equals(actualStatus.getStatus())) {
                // Toss에서는 성공했지만 우리 시스템에서 오류 발생한 경우
                savePayment(actualStatus);
                orderServiceClient.updateOrderStatusToCompleted(orderNumber);
                return actualStatus;
            } else {
                // 실제로 결제 실패한 경우
                throw new RuntimeException("결제 실패: " + originalException.getMessage());
            }
        } catch (Exception e) {
            // 상태 조회도 실패한 경우 - 수동 확인 필요
            throw new RuntimeException("결제 상태 확인 실패 - 수동 확인 필요: " + orderNumber);
        }
    }
}
