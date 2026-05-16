package com.birmarket.service.interfaces;

import com.birmarket.dto.OrderResponse;
import com.birmarket.dto.PlaceOrderRequest;
import com.birmarket.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse placeOrder(PlaceOrderRequest req);
    OrderResponse getOrderById(Long id);
    Page<OrderResponse> getMyOrders(OrderStatus status, Pageable pageable);
    OrderResponse cancelOrder(Long id);
    Page<OrderResponse> getOrdersForSeller(Pageable pageable);
    Page<OrderResponse> getAllOrders(Pageable pageable);
    OrderResponse updateStatus(Long id, OrderStatus newStatus);
}
