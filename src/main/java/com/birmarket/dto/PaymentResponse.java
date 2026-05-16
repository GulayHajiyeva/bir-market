package com.birmarket.dto;

import com.birmarket.entity.Payment;
import com.birmarket.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentResponse {

    private Long id;
    private Long orderId;
    private String orderNumber;
    private PaymentStatus paymentStatus;
    private BigDecimal amount;
    private String iyzicoPaymentId;
    private String errorMessage;
    private LocalDateTime createdAt;

    public static PaymentResponse fromEntity(Payment payment) {
        PaymentResponse resp = new PaymentResponse();
        resp.setId(payment.getId());
        resp.setOrderId(payment.getOrder().getId());
        resp.setOrderNumber(payment.getOrder().getOrderNumber());
        resp.setPaymentStatus(payment.getPaymentStatus());
        resp.setAmount(payment.getAmount());
        resp.setIyzicoPaymentId(payment.getIyzicoPaymentId());
        resp.setErrorMessage(payment.getErrorMessage());
        resp.setCreatedAt(payment.getCreatedAt());
        return resp;
    }
}
