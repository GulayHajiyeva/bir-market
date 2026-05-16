package com.birmarket.controller;

import com.birmarket.dto.ApiResponse;
import com.birmarket.dto.UserResponse;
import com.birmarket.entity.User;
import com.birmarket.repository.OrderRepository;
import com.birmarket.repository.ProductRepository;
import com.birmarket.service.interfaces.UserService;
import com.birmarket.util.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller")
public class SellerController {

    private final UserService userService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getMyProfile()));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<SellerDashboard>> getDashboard() {
        User seller = SecurityHelper.getCurrentUser();

        long myProducts = productRepository.countBySeller(seller);
        long myOrders = orderRepository.findOrdersForSeller(
                seller, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        java.math.BigDecimal myRevenue = orderRepository.getTotalRevenue();

        SellerDashboard dashboard = new SellerDashboard(myProducts, myOrders, myRevenue);
        return ResponseEntity.ok(ApiResponse.ok(dashboard));
    }

    record SellerDashboard(long totalProducts, long totalOrders, java.math.BigDecimal totalRevenue) {}
}
