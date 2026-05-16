package com.birmarket.service;

import com.birmarket.service.impl.CartServiceImpl;
import com.birmarket.dto.AddToCartRequest;
import com.birmarket.dto.CartResponse;
import com.birmarket.entity.*;
import com.birmarket.exception.BadRequestException;
import com.birmarket.exception.ForbiddenException;
import com.birmarket.exception.NotFoundException;
import com.birmarket.repository.CartItemRepository;
import com.birmarket.repository.CartRepository;
import com.birmarket.repository.ProductRepository;
import com.birmarket.util.SecurityHelper;
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
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private MockedStatic<SecurityHelper> securityMock;
    private User customer;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setup() {
        customer = new User();
        customer.setId(1L);
        customer.setRole(Role.CUSTOMER);
        customer.setEmail("customer@test.com");
        customer.setFullName("Test Customer");

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(customer);
        cart.setItems(new ArrayList<>());

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStockQuantity(10);
        product.setActive(true);

        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Test");
        product.setCategory(cat);

        User prodSeller = new User();
        prodSeller.setId(99L);
        prodSeller.setFullName("Seller");
        product.setSeller(prodSeller);

        securityMock = mockStatic(SecurityHelper.class);
        securityMock.when(SecurityHelper::getCurrentUser).thenReturn(customer);
    }

    @AfterEach
    void cleanup() {
        securityMock.close();
    }

    @Test
    void addItem_successfully_adds_new_item() {
        AddToCartRequest req = new AddToCartRequest();
        req.setProductId(1L);
        req.setQuantity(2);

        when(cartRepository.findByUserWithItems(customer)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        CartResponse result = cartService.addItem(req);

        assertNotNull(result);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItem_fails_when_not_enough_stock() {
        product.setStockQuantity(1); // only 1 in stock

        AddToCartRequest req = new AddToCartRequest();
        req.setProductId(1L);
        req.setQuantity(5); // but requesting 5

        when(cartRepository.findByUserWithItems(customer)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> cartService.addItem(req));
        assertTrue(ex.getMessage().contains("Not enough stock"));
    }

    @Test
    void addItem_fails_for_inactive_product() {
        product.setActive(false);

        AddToCartRequest req = new AddToCartRequest();
        req.setProductId(1L);
        req.setQuantity(1);

        when(cartRepository.findByUserWithItems(customer)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> cartService.addItem(req));
    }

    @Test
    void updateItemQuantity_changes_quantity() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(2);
        item.setPriceAtAddition(BigDecimal.valueOf(100));
        cart.getItems().add(item);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartItemRepository.save(any())).thenReturn(item);

        CartResponse result = cartService.updateItemQuantity(1L, 5);

        assertEquals(5, item.getQuantity());
        assertNotNull(result);
    }

    @Test
    void updateItemQuantity_fails_for_zero_quantity() {
        assertThrows(BadRequestException.class, () -> cartService.updateItemQuantity(1L, 0));
    }

    @Test
    void updateItemQuantity_fails_when_not_enough_stock() {
        product.setStockQuantity(3);

        CartItem item = new CartItem();
        item.setId(1L);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(1);
        item.setPriceAtAddition(BigDecimal.valueOf(100));

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class, () -> cartService.updateItemQuantity(1L, 10));
    }

    @Test
    void updateItemQuantity_fails_when_not_your_item() {
        User otherUser = new User();
        otherUser.setId(99L);

        Cart otherCart = new Cart();
        otherCart.setId(2L);
        otherCart.setUser(otherUser);
        otherCart.setItems(new ArrayList<>());

        CartItem item = new CartItem();
        item.setId(5L);
        item.setCart(otherCart);
        item.setProduct(product);
        item.setQuantity(1);

        when(cartItemRepository.findById(5L)).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class, () -> cartService.updateItemQuantity(5L, 2));
    }

    @Test
    void removeItem_removes_from_cart() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(2);
        item.setPriceAtAddition(BigDecimal.valueOf(100));
        cart.getItems().add(item);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));

        cartService.removeItem(1L);

        verify(cartItemRepository).delete(item);
        assertFalse(cart.getItems().contains(item));
    }

    @Test
    void removeItem_fails_when_not_your_item() {
        User otherUser = new User();
        otherUser.setId(99L);

        Cart otherCart = new Cart();
        otherCart.setId(2L);
        otherCart.setUser(otherUser);
        otherCart.setItems(new ArrayList<>());

        CartItem item = new CartItem();
        item.setId(5L);
        item.setCart(otherCart);
        item.setProduct(product);
        item.setQuantity(1);

        when(cartItemRepository.findById(5L)).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class, () -> cartService.removeItem(5L));
    }

    @Test
    void clearCart_removes_everything() {
        // add a fake item to the cart first
        CartItem item = new CartItem();
        item.setId(1L);
        item.setProduct(product);
        item.setQuantity(1);
        item.setPriceAtAddition(BigDecimal.valueOf(100));
        cart.getItems().add(item);

        when(cartRepository.findByUserWithItems(customer)).thenReturn(Optional.of(cart));

        cartService.clearCart();

        verify(cartItemRepository).deleteAllByCart(cart);
        assertTrue(cart.getItems().isEmpty());
    }
}
