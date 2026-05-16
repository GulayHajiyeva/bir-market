package com.birmarket.service.impl;

import com.birmarket.dto.AddToCartRequest;
import com.birmarket.dto.CartResponse;
import com.birmarket.entity.Cart;
import com.birmarket.entity.CartItem;
import com.birmarket.entity.Product;
import com.birmarket.entity.User;
import com.birmarket.exception.BadRequestException;
import com.birmarket.exception.ForbiddenException;
import com.birmarket.exception.NotFoundException;
import com.birmarket.repository.CartItemRepository;
import com.birmarket.repository.CartRepository;
import com.birmarket.repository.ProductRepository;
import com.birmarket.service.interfaces.CartService;
import com.birmarket.util.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    public CartResponse getMyCart() {
        log.info("ActionLog.getMyCart.start");
        User user = SecurityHelper.getCurrentUser();
        Cart cart = getOrCreateCart(user);
        CartResponse response = buildCartResponse(cart);
        log.info("ActionLog.getMyCart.end");
        return response;
    }

    @Override
    @Transactional
    public CartResponse addItem(AddToCartRequest req) {
        log.info("ActionLog.addItem.start");

        User user = SecurityHelper.getCurrentUser();
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found: " + req.getProductId()));

        if (!product.isActive()) {
            throw new BadRequestException("This product is no longer available");
        }

        if (product.getStockQuantity() < req.getQuantity()) {
            throw new BadRequestException("Not enough stock. Only " + product.getStockQuantity() + " left");
        }

        Optional<CartItem> existing = cartItemRepository.findByCartAndProduct(cart, product);

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int totalQty = item.getQuantity() + req.getQuantity();

            if (product.getStockQuantity() < totalQty) {
                throw new BadRequestException("Not enough stock. Only " + product.getStockQuantity() + " available");
            }

            item.setQuantity(totalQty);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(req.getQuantity());
            newItem.setPriceAtAddition(product.getPrice());
            cartItemRepository.save(newItem);
        }

        Cart updatedCart = cartRepository.findByUserWithItems(user).orElse(cart);
        CartResponse response = buildCartResponse(updatedCart);

        log.info("ActionLog.addItem.end");
        return response;
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(Long cartItemId, int newQuantity) {
        log.info("ActionLog.updateItemQuantity.start");

        if (newQuantity < 1) {
            throw new BadRequestException("Quantity must be at least 1. Use remove to delete item");
        }

        User user = SecurityHelper.getCurrentUser();
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found: " + cartItemId));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Not your cart item");
        }

        if (item.getProduct().getStockQuantity() < newQuantity) {
            throw new BadRequestException("Only " + item.getProduct().getStockQuantity() + " in stock");
        }

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
        CartResponse response = buildCartResponse(item.getCart());

        log.info("ActionLog.updateItemQuantity.end");
        return response;
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long cartItemId) {
        log.info("ActionLog.removeItem.start");

        User user = SecurityHelper.getCurrentUser();
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found: " + cartItemId));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Not your cart item");
        }

        Cart cart = item.getCart();
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        CartResponse response = buildCartResponse(cart);

        log.info("ActionLog.removeItem.end");
        return response;
    }

    @Override
    @Transactional
    public CartResponse clearCart() {
        log.info("ActionLog.clearCart.start");

        User user = SecurityHelper.getCurrentUser();
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteAllByCart(cart);
        cart.getItems().clear();
        CartResponse response = buildCartResponse(cart);

        log.info("ActionLog.clearCart.end");
        return response;
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserWithItems(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartResponse.CartItemInfo> itemInfos = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalItems = 0;

        for (CartItem item : cart.getItems()) {
            CartResponse.CartItemInfo info = new CartResponse.CartItemInfo();
            info.setCartItemId(item.getId());
            info.setProductId(item.getProduct().getId());
            info.setProductName(item.getProduct().getName());
            info.setProductImage(item.getProduct().getImageUrl());
            info.setQuantity(item.getQuantity());
            info.setPriceWhenAdded(item.getPriceAtAddition());

            BigDecimal currentPrice = item.getProduct().getPrice();
            info.setCurrentPrice(currentPrice);
            info.setSubtotal(item.getPriceAtAddition().multiply(BigDecimal.valueOf(item.getQuantity())));
            info.setPriceChanged(!currentPrice.equals(item.getPriceAtAddition()));
            info.setOutOfStock(item.getProduct().getStockQuantity() < item.getQuantity());

            totalPrice = totalPrice.add(info.getSubtotal());
            totalItems += item.getQuantity();
            itemInfos.add(info);
        }

        CartResponse resp = new CartResponse();
        resp.setCartId(cart.getId());
        resp.setItems(itemInfos);
        resp.setTotalPrice(totalPrice);
        resp.setTotalItems(totalItems);
        return resp;
    }
}
