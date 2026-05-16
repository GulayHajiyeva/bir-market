package com.birmarket.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StatsResponse {

    private long totalUsers;
    private long totalSellers;
    private long totalCustomers;
    private long blockedUsers;
    private long totalProducts;
    private long outOfStockProducts;
    private long totalCategories;
    private long totalOrders;
    private long pendingOrders;
    private long deliveredOrders;
    private BigDecimal totalRevenue;
}
