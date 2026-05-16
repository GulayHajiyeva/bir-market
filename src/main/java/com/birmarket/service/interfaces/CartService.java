package com.birmarket.service.interfaces;

import com.birmarket.dto.AddToCartRequest;
import com.birmarket.dto.CartResponse;

public interface CartService {
    CartResponse getMyCart();
    CartResponse addItem(AddToCartRequest req);
    CartResponse updateItemQuantity(Long cartItemId, int newQuantity);
    CartResponse removeItem(Long cartItemId);
    CartResponse clearCart();
}
