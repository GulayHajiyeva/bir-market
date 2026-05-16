package com.birmarket.service;

import com.birmarket.service.impl.PaymentServiceImpl;
import com.birmarket.dto.PaymentRequest;
import com.birmarket.dto.PaymentResponse;
import com.birmarket.entity.*;
import com.birmarket.exception.BadRequestException;
import com.birmarket.exception.ForbiddenException;
import com.birmarket.exception.NotFoundException;
import com.birmarket.repository.OrderRepository;
import com.birmarket.repository.PaymentRepository;
import com.birmarket.util.SecurityHelper;
import com.iyzipay.Options;
import com.iyzipay.request.CreatePaymentRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private Options iyzicoOptions;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private MockedStatic<SecurityHelper> securityMock;
    private MockedStatic<com.iyzipay.model.Payment> iyzicoPaymentMock;
    private User customer;
    private User otherCustomer;
    private Order order;
    private PaymentRequest paymentReq;

    @BeforeEach
    void setup() {
        customer = new User();
        customer.setId(1L);
        customer.setEmail("customer@test.com");
        customer.setFullName("Test Customer");
        customer.setRole(Role.CUSTOMER);

        otherCustomer = new User();
        otherCustomer.setId(2L);
        otherCustomer.setEmail("other@test.com");
        otherCustomer.setRole(Role.CUSTOMER);

        order = new Order();
        order.setId(1L);
        order.setOrderNumber("BM-001");
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(200));
        order.setShippingAddress("Test Address, Baku");
        order.setOrderItems(new ArrayList<>());

        paymentReq = new PaymentRequest();
        paymentReq.setCardHolderName("Test Customer");
        paymentReq.setCardNumber("5528790000000008");
        paymentReq.setExpireMonth("12");
        paymentReq.setExpireYear("2030");
        paymentReq.setCvc("123");

        securityMock = mockStatic(SecurityHelper.class);
        securityMock.when(SecurityHelper::getCurrentUser).thenReturn(customer);

        iyzicoPaymentMock = mockStatic(com.iyzipay.model.Payment.class);
    }

    @AfterEach
    void cleanup() {
        securityMock.close();
        iyzicoPaymentMock.close();
    }

    @Test
    void payForOrder_fails_when_order_not_found() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> paymentService.payForOrder(99L, paymentReq));
    }

    @Test
    void payForOrder_fails_when_not_your_order() {
        securityMock.when(SecurityHelper::getCurrentUser).thenReturn(otherCustomer);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ForbiddenException.class, () -> paymentService.payForOrder(1L, paymentReq));
    }

    @Test
    void payForOrder_fails_when_order_not_pending() {
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> paymentService.payForOrder(1L, paymentReq));
    }

    @Test
    void payForOrder_fails_when_already_paid() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderAndPaymentStatus(order, PaymentStatus.PAID)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> paymentService.payForOrder(1L, paymentReq));
    }

    @Test
    void payForOrder_succeeds_when_iyzico_returns_success() {
        com.iyzipay.model.Payment iyzicoResult = mock(com.iyzipay.model.Payment.class);
        when(iyzicoResult.getStatus()).thenReturn("success");
        when(iyzicoResult.getPaymentId()).thenReturn("iyzico-456");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderAndPaymentStatus(order, PaymentStatus.PAID)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        iyzicoPaymentMock.when(() -> com.iyzipay.model.Payment.create(
                any(CreatePaymentRequest.class), any(Options.class))).thenReturn(iyzicoResult);

        PaymentResponse result = paymentService.payForOrder(1L, paymentReq);

        assertNotNull(result);
        assertEquals(PaymentStatus.PAID, result.getPaymentStatus());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void payForOrder_marks_failed_when_iyzico_declines() {
        com.iyzipay.model.Payment iyzicoResult = mock(com.iyzipay.model.Payment.class);
        when(iyzicoResult.getStatus()).thenReturn("failure");
        when(iyzicoResult.getErrorMessage()).thenReturn("Insufficient funds");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderAndPaymentStatus(order, PaymentStatus.PAID)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        iyzicoPaymentMock.when(() -> com.iyzipay.model.Payment.create(
                any(CreatePaymentRequest.class), any(Options.class))).thenReturn(iyzicoResult);

        PaymentResponse result = paymentService.payForOrder(1L, paymentReq);

        assertNotNull(result);
        assertEquals(PaymentStatus.FAILED, result.getPaymentStatus());
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void getPaymentByOrderId_returns_payment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrder(order);
        payment.setAmount(BigDecimal.valueOf(200));
        payment.setPaymentStatus(PaymentStatus.PAID);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));

        PaymentResponse result = paymentService.getPaymentByOrderId(1L);

        assertNotNull(result);
        assertEquals(PaymentStatus.PAID, result.getPaymentStatus());
    }

    @Test
    void getPaymentByOrderId_fails_for_wrong_customer() {
        securityMock.when(SecurityHelper::getCurrentUser).thenReturn(otherCustomer);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ForbiddenException.class, () -> paymentService.getPaymentByOrderId(1L));
    }

    @Test
    void getPaymentByOrderId_admin_can_view_any_order() {
        User admin = new User();
        admin.setId(99L);
        admin.setRole(Role.ADMIN);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrder(order);
        payment.setAmount(BigDecimal.valueOf(200));
        payment.setPaymentStatus(PaymentStatus.PAID);

        securityMock.when(SecurityHelper::getCurrentUser).thenReturn(admin);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));

        PaymentResponse result = paymentService.getPaymentByOrderId(1L);

        assertNotNull(result);
        assertEquals(PaymentStatus.PAID, result.getPaymentStatus());
    }
}
