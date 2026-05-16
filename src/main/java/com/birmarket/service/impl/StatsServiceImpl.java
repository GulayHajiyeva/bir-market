package com.birmarket.service.impl;

import com.birmarket.dto.StatsResponse;
import com.birmarket.entity.OrderStatus;
import com.birmarket.entity.Role;
import com.birmarket.repository.CategoryRepository;
import com.birmarket.repository.OrderRepository;
import com.birmarket.repository.ProductRepository;
import com.birmarket.repository.UserRepository;
import com.birmarket.service.interfaces.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public StatsResponse getSystemStats() {
        log.info("ActionLog.getSystemStats.start");

        StatsResponse stats = new StatsResponse();

        stats.setTotalUsers(userRepository.count());
        stats.setTotalSellers(userRepository.countByRole(Role.SELLER));
        stats.setTotalCustomers(userRepository.countByRole(Role.CUSTOMER));
        stats.setBlockedUsers(userRepository.countByBlocked(true));

        stats.setTotalProducts(productRepository.countByActiveTrue());
        stats.setOutOfStockProducts(productRepository.countByActiveTrueAndStockQuantity(0));
        stats.setTotalCategories(categoryRepository.count());

        stats.setTotalOrders(orderRepository.count());
        stats.setPendingOrders(orderRepository.countByStatus(OrderStatus.PENDING));
        stats.setDeliveredOrders(orderRepository.countByStatus(OrderStatus.DELIVERED));

        stats.setTotalRevenue(orderRepository.getTotalRevenue());

        log.info("ActionLog.getSystemStats.end");
        return stats;
    }
}
