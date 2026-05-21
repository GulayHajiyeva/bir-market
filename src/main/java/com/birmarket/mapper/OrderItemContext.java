package com.birmarket.mapper;

import com.birmarket.entity.Product;
import com.birmarket.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderItemContext {
    private Product product;
    private User seller;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
