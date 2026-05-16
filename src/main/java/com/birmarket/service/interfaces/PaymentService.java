package com.birmarket.service.interfaces;

import com.birmarket.dto.PaymentRequest;
import com.birmarket.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse payForOrder(Long orderId, PaymentRequest req);
    PaymentResponse getPaymentByOrderId(Long orderId);
}
