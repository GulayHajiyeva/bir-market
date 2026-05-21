package com.birmarket.service;

import com.birmarket.dto.OrderResponse;
import com.birmarket.dto.PlaceOrderRequest;
import com.birmarket.entity.*;
import com.birmarket.enums.OrderStatus;
import com.birmarket.enums.Role;
import com.birmarket.exception.BadRequestException;
import com.birmarket.mapper.OrderMapper;
import com.birmarket.repository.*;
import com.birmarket.service.impl.OrderServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private MockedStatic<SecurityHelper> securityMock;

    private User customer;
    private User seller;
    private Product product;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setup() {

        customer = new User();
        customer.setId(1L);
        customer.setEmail("customer@test.com");
        customer.setFullName("Test Customer");
        customer.setRole(Role.CUSTOMER);

        seller = new User();
        seller.setId(2L);
        seller.setFullName("Test Seller");
        seller.setRole(Role.SELLER);

        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        product = new Product();
        product.setId(1L);
        product.setName("Phone");
        product.setPrice(BigDecimal.valueOf(200));
        product.setStockQuantity(5);
        product.setActive(true);
        product.setCategory(category);
        product.setSeller(seller);

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(customer);
        cart.setItems(new ArrayList<>());

        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setPriceAtAddition(BigDecimal.valueOf(200));

        cart.getItems().add(cartItem);

        securityMock = mockStatic(SecurityHelper.class);
        securityMock.when(SecurityHelper::getCurrentUser)
                .thenReturn(customer);
    }

    @AfterEach
    void cleanup() {
        securityMock.close();
    }

    @Test
    void placeOrder_works_and_reduces_stock() {

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setShippingAddress("123 Test Street, Baku");

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setOrderNumber("BM-TEST-001");
        savedOrder.setCustomer(customer);
        savedOrder.setTotalAmount(BigDecimal.valueOf(400));
        savedOrder.setOrderItems(new ArrayList<>());

        OrderResponse response = new OrderResponse();
        response.setId(1L);
        response.setOrderNumber("BM-TEST-001");
        response.setStatus(OrderStatus.PENDING);

        when(cartRepository.findByUser(customer))
                .thenReturn(Optional.of(cart));

        when(productRepository.findByIdForUpdate(product.getId()))
                .thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);

        when(orderMapper.toResponse(any(Order.class)))
                .thenReturn(response);

        OrderResponse result = orderService.placeOrder(req);

        assertNotNull(result);
        assertEquals(3, product.getStockQuantity());

        verify(cartItemRepository).deleteAllByCart(cart);
        verify(productRepository).save(product);
        verify(orderRepository, times(2)).save(any(Order.class));
    }

    @Test
    void placeOrder_fails_with_empty_cart() {

        cart.getItems().clear();

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setShippingAddress("123 Test Street");

        when(cartRepository.findByUser(customer))
                .thenReturn(Optional.of(cart));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> orderService.placeOrder(req)
        );

        assertEquals("Your cart is empty", ex.getMessage());

        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_fails_when_not_enough_stock() {

        product.setStockQuantity(1);

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setShippingAddress("Test address");

        when(cartRepository.findByUser(customer))
                .thenReturn(Optional.of(cart));

        when(productRepository.findByIdForUpdate(product.getId()))
                .thenReturn(Optional.of(product));

        assertThrows(
                BadRequestException.class,
                () -> orderService.placeOrder(req)
        );

        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_restores_stock() {

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setSeller(seller);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(200));
        orderItem.setSubtotal(BigDecimal.valueOf(400));

        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(List.of(orderItem));

        orderItem.setOrder(order);

        product.setStockQuantity(3);

        OrderResponse response = new OrderResponse();
        response.setId(1L);
        response.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        when(orderMapper.toResponse(order))
                .thenReturn(response);

        OrderResponse result = orderService.cancelOrder(1L);

        assertNotNull(result);
        assertEquals(5, product.getStockQuantity());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        verify(productRepository).save(product);
    }

    @Test
    void cancelOrder_fails_for_shipped_order() {

        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.SHIPPED);
        order.setOrderItems(new ArrayList<>());

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                BadRequestException.class,
                () -> orderService.cancelOrder(1L)
        );

        verify(orderRepository, never()).save(any());
    }
}