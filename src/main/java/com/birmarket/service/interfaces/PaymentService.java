package com.birmarket.service.interfaces;

import com.birmarket.dto.ApiResponse;
import com.birmarket.dto.PaymentRequest;
import com.birmarket.dto.PaymentResponse;
import org.springframework.http.ResponseEntity;

public interface PaymentService {
    ResponseEntity<ApiResponse<PaymentResponse>> payForOrder(
            Long orderId,
            PaymentRequest req);    PaymentResponse getPaymentByOrderId(Long orderId);
}
