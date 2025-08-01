package com.nhnacademy.illuwa.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "order-service")
public interface OrderServiceClient {
    @PostMapping("/api/order/common/payment-success/{order-number}")
    void updateOrderStatusToCompleted(@PathVariable("order-number") String orderNumber);
}
