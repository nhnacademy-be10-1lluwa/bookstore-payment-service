package com.nhnacademy.illuwa.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.illuwa.domain.payment.dto.PaymentResponse;
import com.nhnacademy.illuwa.domain.payment.dto.RefundRequest;
import com.nhnacademy.illuwa.domain.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
public class PaymentController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String API_SECRET_KEY = "test_sk_6BYq7GWPVvGwommJE6pnrNE5vbo1";
    private final PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;


    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Json파일을 받아서 저장
    @RequestMapping("/confirm/payment")
    @ResponseBody
    public ResponseEntity<JSONObject> confirmPayment(@RequestBody String jsonBody) throws Exception {
        // response -> json 객체
        JSONObject response = sendRequest(parseRequestData(jsonBody), API_SECRET_KEY, "https://api.tosspayments.com/v1/payments/confirm");

        if (!response.containsKey("error")) {
            // 객체 -> Json 문자열 -> java 객체
            PaymentResponse paymentResponse = objectMapper.readValue(response.toJSONString(), PaymentResponse.class);
            paymentService.savePayment(paymentResponse);
        }

        int statusCode = response.containsKey("error") ? 400 : 200;
        return ResponseEntity.status(statusCode).body(response);
    }

    @RequestMapping(value = "/callback-auth", method = RequestMethod.GET)
    public ResponseEntity<JSONObject> callbackAuth(@RequestParam String customerKey, @RequestParam String code) throws Exception {
        // JSONObject -> json 데이터를 자바에서 객체처럼 다루기 위해 사용
        JSONObject requestData = new JSONObject();
        requestData.put("grantType", "AuthorizationCode");
        requestData.put("customerKey", customerKey);
        requestData.put("code", code);
        
        String url = "https://api.tosspayments.com/v1/brandpay/authorizations/access-token";
        JSONObject response = sendRequest(requestData, API_SECRET_KEY, url);

        logger.info("Response Data: {}", response);

        return ResponseEntity.status(response.containsKey("error") ? 400 : 200).body(response);
    }

    private JSONObject parseRequestData(String jsonBody) {
        try {
            return (JSONObject) new JSONParser().parse(jsonBody);
        } catch (ParseException e) {
            logger.error("JSON Parsing Error", e);
            return new JSONObject();
        }
    }

    private JSONObject sendRequest(JSONObject requestData, String secretKey, String urlString) throws IOException {
        HttpURLConnection connection = createConnection(secretKey, urlString);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestData.toString().getBytes(StandardCharsets.UTF_8));
        }

        try (InputStream responseStream = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
             Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
            return (JSONObject) new JSONParser().parse(reader);
        } catch (Exception e) {
            logger.error("Error reading response", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "Error reading response");
            return errorResponse;
        }
    }

    private HttpURLConnection createConnection(String secretKey, String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        return connection;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {

        return "/payment/checkout";
    }

    @RequestMapping(value = "/fail", method = RequestMethod.GET)
    public String failPayment(HttpServletRequest request, Model model) {
        model.addAttribute("code", request.getParameter("code"));
        model.addAttribute("message", request.getParameter("message"));
        return "/fail";
    }

      // @RestController일 경우
//    @GetMapping("/orders/{orderId}")
    @GetMapping("/v1/payments/orders/{orderId}")
    @ResponseBody
    public ResponseEntity<PaymentResponse> findPaymentByOrderId(@PathVariable String orderId) {
        PaymentResponse resp = paymentService.findPaymentByOrderId(orderId);
        return ResponseEntity.ok(resp); // 반드시 @ResponseBody 필요
    }

    // 환불 (OrderId)
    @PostMapping("/v1/payments/orders/{orderId}/cancel")
    public ResponseEntity<PaymentResponse> cancelByOrderId(
            @PathVariable String orderId,
            @RequestBody @Valid RefundRequest refundRequest) {

        PaymentResponse rep = paymentService.findPaymentByOrderId(orderId);

        refundRequest.setPaymentKey(rep.getPaymentKey());

        PaymentResponse response = paymentService.cancelPayment(refundRequest);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/payment/test")
    public String testPage() {
        return "payment/test";
    }
}
