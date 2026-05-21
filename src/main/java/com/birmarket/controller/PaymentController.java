package com.birmarket.controller;

import com.birmarket.dto.ApiResponse;
import com.birmarket.dto.PaymentRequest;
import com.birmarket.dto.PaymentResponse;
import com.birmarket.enums.PaymentStatus;
import com.birmarket.service.interfaces.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    @PostMapping("/pay/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> payForOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequest req) {

        return paymentService.payForOrder(orderId, req);
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getPaymentByOrderId(orderId)));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody(required = false) String payload) {
        return ResponseEntity.ok("OK");
    }
}
