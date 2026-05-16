package com.birmarket.service;

import com.birmarket.service.impl.StatsServiceImpl;
import com.birmarket.dto.StatsResponse;
import com.birmarket.entity.OrderStatus;
import com.birmarket.entity.Role;
import com.birmarket.repository.CategoryRepository;
import com.birmarket.repository.OrderRepository;
import com.birmarket.repository.ProductRepository;
import com.birmarket.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    @Test
    void getSystemStats_aggregates_all_data() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByRole(Role.SELLER)).thenReturn(3L);
        when(userRepository.countByRole(Role.CUSTOMER)).thenReturn(6L);
        when(userRepository.countByBlocked(true)).thenReturn(1L);
        when(productRepository.countByActiveTrue()).thenReturn(25L);
        when(productRepository.countByActiveTrueAndStockQuantity(0)).thenReturn(2L);
        when(categoryRepository.count()).thenReturn(5L);
        when(orderRepository.count()).thenReturn(50L);
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(8L);
        when(orderRepository.countByStatus(OrderStatus.DELIVERED)).thenReturn(30L);
        when(orderRepository.getTotalRevenue()).thenReturn(BigDecimal.valueOf(9999.99));

        StatsResponse result = statsService.getSystemStats();

        assertEquals(10L, result.getTotalUsers());
        assertEquals(3L, result.getTotalSellers());
        assertEquals(6L, result.getTotalCustomers());
        assertEquals(1L, result.getBlockedUsers());
        assertEquals(25L, result.getTotalProducts());
        assertEquals(2L, result.getOutOfStockProducts());
        assertEquals(5L, result.getTotalCategories());
        assertEquals(50L, result.getTotalOrders());
        assertEquals(8L, result.getPendingOrders());
        assertEquals(30L, result.getDeliveredOrders());
        assertEquals(BigDecimal.valueOf(9999.99), result.getTotalRevenue());
    }
}
