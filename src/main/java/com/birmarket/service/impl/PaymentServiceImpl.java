package com.birmarket.service.impl;

import com.birmarket.dto.PaymentRequest;
import com.birmarket.dto.PaymentResponse;
import com.birmarket.entity.Order;
import com.birmarket.entity.OrderItem;
import com.birmarket.entity.OrderStatus;
import com.birmarket.entity.Payment;
import com.birmarket.entity.PaymentStatus;
import com.birmarket.entity.Role;
import com.birmarket.entity.User;
import com.birmarket.exception.BadRequestException;
import com.birmarket.exception.ForbiddenException;
import com.birmarket.exception.NotFoundException;
import com.birmarket.repository.OrderRepository;
import com.birmarket.repository.PaymentRepository;
import com.birmarket.service.interfaces.PaymentService;
import com.birmarket.util.SecurityHelper;
import com.iyzipay.Options;
import com.iyzipay.model.Address;
import com.iyzipay.model.BasketItem;
import com.iyzipay.model.BasketItemType;
import com.iyzipay.model.Buyer;
import com.iyzipay.model.Currency;
import com.iyzipay.model.Locale;
import com.iyzipay.model.PaymentCard;
import com.iyzipay.model.PaymentChannel;
import com.iyzipay.model.PaymentGroup;
import com.iyzipay.request.CreatePaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final Options iyzicoOptions;

    @Override
    @Transactional
    public PaymentResponse payForOrder(Long orderId, PaymentRequest req) {
        log.info("ActionLog.payForOrder.start");

        User customer = SecurityHelper.getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenException("This is not your order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in PENDING status, current status: " + order.getStatus());
        }

        if (paymentRepository.existsByOrderAndPaymentStatus(order, PaymentStatus.PAID)) {
            throw new BadRequestException("This order is already paid");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setConversationId(UUID.randomUUID().toString());
        payment = paymentRepository.save(payment);

        CreatePaymentRequest iyzicoRequest = buildIyzicoRequest(order, customer, req, payment.getConversationId());
        com.iyzipay.model.Payment iyzicoResult = com.iyzipay.model.Payment.create(iyzicoRequest, iyzicoOptions);

        if ("success".equals(iyzicoResult.getStatus())) {
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setIyzicoPaymentId(iyzicoResult.getPaymentId());
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setErrorMessage(iyzicoResult.getErrorMessage());
        }

        payment = paymentRepository.save(payment);
        PaymentResponse response = PaymentResponse.fromEntity(payment);

        log.info("ActionLog.payForOrder.end");
        return response;
    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        log.info("ActionLog.getPaymentByOrderId.start");

        User currentUser = SecurityHelper.getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (!order.getCustomer().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("This is not your order");
        }

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new NotFoundException("No payment found for this order"));

        PaymentResponse response = PaymentResponse.fromEntity(payment);

        log.info("ActionLog.getPaymentByOrderId.end");
        return response;
    }

    private CreatePaymentRequest buildIyzicoRequest(Order order, User customer,
                                                    PaymentRequest req, String conversationId) {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setLocale(Locale.TR.getValue());
        request.setConversationId(conversationId);

        BigDecimal price = order.getTotalAmount().setScale(2, RoundingMode.HALF_UP);
        request.setPrice(price);
        request.setPaidPrice(price);

        request.setCurrency(Currency.TRY.name());
        request.setInstallment(1);
        request.setBasketId(order.getOrderNumber());
        request.setPaymentChannel(PaymentChannel.WEB.name());
        request.setPaymentGroup(PaymentGroup.PRODUCT.name());

        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setCardHolderName(req.getCardHolderName());
        paymentCard.setCardNumber(req.getCardNumber());
        paymentCard.setExpireMonth(req.getExpireMonth());
        paymentCard.setExpireYear(req.getExpireYear());
        paymentCard.setCvc(req.getCvc());
        paymentCard.setRegisterCard(0);
        request.setPaymentCard(paymentCard);

        Buyer buyer = new Buyer();
        buyer.setId(String.valueOf(customer.getId()));
        buyer.setName(getFirstName(customer.getFullName()));
        buyer.setSurname(getLastName(customer.getFullName()));
        buyer.setEmail(customer.getEmail());
        buyer.setIdentityNumber("11111111111");
        buyer.setGsmNumber(customer.getPhone() != null ? customer.getPhone() : "+905555555555");
        buyer.setRegistrationAddress(order.getShippingAddress());
        buyer.setCity("Istanbul");
        buyer.setCountry("Turkey");
        buyer.setIp("85.34.78.112");
        request.setBuyer(buyer);

        Address address = new Address();
        address.setContactName(customer.getFullName());
        address.setCity("Istanbul");
        address.setCountry("Turkey");
        address.setAddress(order.getShippingAddress());
        request.setShippingAddress(address);
        request.setBillingAddress(address);

        List<BasketItem> basketItems = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            BasketItem basketItem = new BasketItem();
            basketItem.setId(String.valueOf(item.getId()));
            basketItem.setName(item.getProduct().getName());
            basketItem.setCategory1("General");
            basketItem.setItemType(BasketItemType.PHYSICAL.name());
            basketItem.setPrice(item.getSubtotal().setScale(2, RoundingMode.HALF_UP));
            basketItems.add(basketItem);
        }
        request.setBasketItems(basketItems);

        return request;
    }

    private String getFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "Unknown";
        String[] parts = fullName.trim().split(" ", 2);
        return parts[0];
    }

    private String getLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "Unknown";
        String[] parts = fullName.trim().split(" ", 2);
        return parts.length > 1 ? parts[1] : "Unknown";
    }
}
