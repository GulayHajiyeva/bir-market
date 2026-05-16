package com.birmarket.controller;

import com.birmarket.dto.AddToCartRequest;
import com.birmarket.dto.ApiResponse;
import com.birmarket.dto.CartResponse;
import com.birmarket.service.interfaces.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> viewCart() {
        return ResponseEntity.ok(ApiResponse.ok(cartService.getMyCart()));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(@Valid @RequestBody AddToCartRequest req) {
        CartResponse cart = cartService.addItem(req);
        return ResponseEntity.ok(ApiResponse.ok("Added to cart", cart));
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @PathVariable Long itemId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.updateItemQuantity(itemId, quantity)));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.ok("Item removed", cartService.removeItem(itemId)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<CartResponse>> clearCart() {
        return ResponseEntity.ok(ApiResponse.ok("Cart cleared", cartService.clearCart()));
    }
}
