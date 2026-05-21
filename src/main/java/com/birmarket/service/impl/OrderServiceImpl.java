package com.birmarket.service.impl;

import com.birmarket.dto.OrderResponse;
import com.birmarket.dto.PlaceOrderRequest;
import com.birmarket.entity.*;
import com.birmarket.enums.OrderStatus;
import com.birmarket.enums.Role;
import com.birmarket.exception.BadRequestException;
import com.birmarket.exception.ForbiddenException;
import com.birmarket.exception.NotFoundException;
import com.birmarket.mapper.OrderMapper;
import com.birmarket.repository.*;
import com.birmarket.service.interfaces.OrderService;
import com.birmarket.util.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest req) {

        log.info("ActionLog.placeOrder.start");

        User customer = SecurityHelper.getCurrentUser();

        Cart cart = cartRepository.findByUser(customer)
                .orElseThrow(() -> new BadRequestException("You have no cart"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal orderTotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {

            Product product = productRepository.findByIdForUpdate(
                    cartItem.getProduct().getId()
            ).orElseThrow(() -> new NotFoundException("Product not found"));

            if (!product.isActive()) {
                throw new BadRequestException(
                        "Product '" + product.getName() + "' is no longer available"
                );
            }

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException(
                        "Not enough stock for '" + product.getName() + "'"
                );
            }

            BigDecimal subtotal =
                    product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            orderTotal = orderTotal.add(subtotal);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setSeller(product.getSeller());
            item.setQuantity(cartItem.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setSubtotal(subtotal);

            orderItems.add(item);
        }

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomer(customer);
        order.setTotalAmount(orderTotal);
        order.setShippingAddress(req.getShippingAddress());
        order.setNotes(req.getNotes());
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
        }

        savedOrder.setOrderItems(orderItems);
        savedOrder = orderRepository.save(savedOrder);

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        cartItemRepository.deleteAllByCart(cart);
        cart.getItems().clear();

        log.info("ActionLog.placeOrder.end");

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long id) {

        log.info("ActionLog.getOrderById.start");

        Order order = findOrderByIdWithDetails(id);
        checkOrderAccess(order);

        log.info("ActionLog.getOrderById.end");

        return orderMapper.toResponse(order);
    }

    @Override
    public Page<OrderResponse> getMyOrders(OrderStatus status, Pageable pageable) {

        log.info("ActionLog.getMyOrders.start");

        User customer = SecurityHelper.getCurrentUser();

        Page<Order> orders =
                (status != null)
                        ? orderRepository.findByCustomerAndStatus(customer, status, pageable)
                        : orderRepository.findByCustomer(customer, pageable);

        log.info("ActionLog.getMyOrders.end");

        return orders.map(orderMapper::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id) {

        log.info("ActionLog.cancelOrder.start");

        User customer = SecurityHelper.getCurrentUser();

        Order order = findOrderById(id);

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenException("This is not your order");
        }

        if (order.getStatus() != OrderStatus.PENDING &&
                order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Cannot cancel order with status: " + order.getStatus());
        }

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("ActionLog.cancelOrder.end");

        return orderMapper.toResponse(order);
    }

    @Override
    public Page<OrderResponse> getOrdersForSeller(Pageable pageable) {

        log.info("ActionLog.getOrdersForSeller.start");

        User seller = SecurityHelper.getCurrentUser();

        log.info("ActionLog.getOrdersForSeller.end");

        return orderRepository.findOrdersForSeller(seller, pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {

        log.info("ActionLog.getAllOrders.start");

        log.info("ActionLog.getAllOrders.end");

        return orderRepository.findAll(pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {

        log.info("ActionLog.updateStatus.start");

        Order order = findOrderById(id);

        checkOrderAccess(order);

        validateStatusChange(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        orderRepository.save(order);

        log.info("ActionLog.updateStatus.end");

        return orderMapper.toResponse(order);
    }

    private Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    private Order findOrderByIdWithDetails(Long id) {
        return orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    private void checkOrderAccess(Order order) {
        User current = SecurityHelper.getCurrentUser();

        if (current.getRole() == Role.ADMIN) return;

        if (current.getRole() == Role.CUSTOMER) {
            if (!order.getCustomer().getId().equals(current.getId())) {
                throw new ForbiddenException("This is not your order");
            }
            return;
        }

        if (current.getRole() == Role.SELLER) {
            boolean hasItems = order.getOrderItems().stream()
                    .anyMatch(i -> i.getSeller().getId().equals(current.getId()));

            if (!hasItems) {
                throw new ForbiddenException("You don't have products in this order");
            }
        }
    }

    private void validateStatusChange(OrderStatus current, OrderStatus next) {

        boolean valid =
                (current == OrderStatus.PENDING && next == OrderStatus.CONFIRMED) ||
                        (current == OrderStatus.PENDING && next == OrderStatus.CANCELLED) ||
                        (current == OrderStatus.CONFIRMED && next == OrderStatus.PROCESSING) ||
                        (current == OrderStatus.CONFIRMED && next == OrderStatus.CANCELLED) ||
                        (current == OrderStatus.PROCESSING && next == OrderStatus.SHIPPED) ||
                        (current == OrderStatus.SHIPPED && next == OrderStatus.DELIVERED) ||
                        (current == OrderStatus.DELIVERED && next == OrderStatus.REFUNDED);

        if (!valid) {
            throw new BadRequestException("Cannot change status from " + current + " to " + next);
        }
    }

    private String generateOrderNumber() {

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String randomPart = UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase();

        return "BM-" + timestamp + "-" + randomPart;
    }
}