package com.nhnacademy.illuwa.domain.payment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

public class TossPaymentServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TossPaymentServiceImpl tossPaymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // SecretKey 수동 주입 (@Value는 테스트에서 주입 안 됨)
        tossPaymentService = new TossPaymentServiceImpl(restTemplate, objectMapper);
    }

    @Test
    void fetchPaymentByOrderId_shouldReturnPaymentResponse() throws Exception {
        // given
        String orderId = "ORDER123";
        String apiUrl = "https://api.tosspayments.com/v1/payments/orders/" + orderId;

        String jsonResponse = "{ \"orderId\": \"ORDER123\", \"status\": \"SUCCESS\", \"totalAmount\": 10000 }";

        ResponseEntity<String> mockResponse = new ResponseEntity<>(jsonResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(new URI(apiUrl)),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        PaymentResponse mockPaymentResponse = new PaymentResponse();
        mockPaymentResponse.setOrderId(orderId);
        mockPaymentResponse.setStatus("SUCCESS");
        mockPaymentResponse.setTotalAmount(10000);

        when(objectMapper.readValue(jsonResponse, PaymentResponse.class)).thenReturn(mockPaymentResponse);

        // when
        PaymentResponse result = tossPaymentService.fetchPaymentByOrderId(orderId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo("ORDER123");
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getTotalAmount()).isEqualTo(10000);

        verify(restTemplate, times(1)).exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(String.class));
        verify(objectMapper, times(1)).readValue(jsonResponse, PaymentResponse.class);
    }

    @Test
    void fetchPaymentByOrderId_응답실패_예외발생() throws Exception {
        // given
        String orderId = "ORDER_FAIL";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> failedResponse =
                new ResponseEntity<>("{}", HttpStatus.BAD_REQUEST); // 400 = 실패 응답

        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(failedResponse);

        // when + then
        assertThrows(RuntimeException.class, () -> {
            tossPaymentService.fetchPaymentByOrderId(orderId);
        });
    }

}
