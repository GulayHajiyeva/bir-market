package com.birmarket.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CartResponse {

    private Long cartId;
    private List<CartItemInfo> items;
    private BigDecimal totalPrice;
    private int totalItems;

    @Getter
    @Setter
    public static class CartItemInfo {
        private Long cartItemId;
        private Long productId;
        private String productName;
        private String productImage;
        private int quantity;
        private BigDecimal priceWhenAdded;
        private BigDecimal currentPrice;
        private BigDecimal subtotal;
        private boolean priceChanged;
        private boolean outOfStock;
    }
}
