package com.nhnacademy.illuwa.domain.payment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.service.TossPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TossPaymentServiceImpl implements TossPaymentService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // application-dev.yml secret-key
    @Value("${toss.secret-key}")
    private String secretKey;

    // 수정해야함
//    @Override
//    public PaymentResponse confirmPayment(String paymentKey, String orderId, int amount) {
//        Map<String, Object> body = Map.of(
//                "paymentKey", paymentKey,
//                "orderId", orderId,
//                "amount", amount
//        );
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, createHeaders());
//
//        ResponseEntity<String> resp = restTemplate.postForEntity(
//                "http://api.tosspayments.com/v1/payments/confirm",
//                entity, String.class
//        );
//
//        if (resp.getStatusCode().is2xxSuccessful()) {
//            throw new RuntimeException("결제 승인 실패 : " + resp.getStatusCode());
//        }
//
//        return null;
//    }

    @Override
    public PaymentResponse fetchPaymentByOrderId(String orderId) {
        try {
            URI uri = new URI("https://api.tosspayments.com/v1/payments/orders/" + orderId);
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("결제 정보 조회 실패: " + response.getStatusCode());
            }
            return objectMapper.readValue(response.getBody(), PaymentResponse.class);

        } catch (Exception e) {
            throw new RuntimeException("결제 정보 조회 중 오류 발생", e);
        }
    }

    // Toss Api를 보낼 떄 필요한 HTTP 요청 헤더를 생성
    private HttpHeaders createHeaders() {
        String credential = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));


        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + credential);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
