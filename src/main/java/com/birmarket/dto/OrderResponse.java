package com.birmarket.dto;

import com.birmarket.entity.Order;
import com.birmarket.entity.OrderItem;
import com.birmarket.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String notes;
    private String customerName;
    private List<OrderItemInfo> items;
    private LocalDateTime createdAt;

    @Getter
    @Setter
    public static class OrderItemInfo {
        private Long productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String sellerName;
    }

    public static OrderResponse fromEntity(Order order) {
        OrderResponse resp = new OrderResponse();
        resp.setId(order.getId());
        resp.setOrderNumber(order.getOrderNumber());
        resp.setStatus(order.getStatus());
        resp.setTotalAmount(order.getTotalAmount());
        resp.setShippingAddress(order.getShippingAddress());
        resp.setNotes(order.getNotes());
        resp.setCustomerName(order.getCustomer().getFullName());
        resp.setCreatedAt(order.getCreatedAt());

        List<OrderItemInfo> itemInfos = order.getOrderItems().stream().map(item -> {
            OrderItemInfo info = new OrderItemInfo();
            info.setProductId(item.getProduct().getId());
            info.setProductName(item.getProduct().getName());
            info.setQuantity(item.getQuantity());
            info.setUnitPrice(item.getUnitPrice());
            info.setSubtotal(item.getSubtotal());
            info.setSellerName(item.getSeller().getFullName());
            return info;
        }).collect(Collectors.toList());

        resp.setItems(itemInfos);
        return resp;
    }
}
