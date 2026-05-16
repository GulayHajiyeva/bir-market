package com.birmarket.repository;

import com.birmarket.entity.Order;
import com.birmarket.enums.OrderStatus;
import com.birmarket.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = {"customer", "orderItems", "orderItems.product", "orderItems.seller"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"customer", "orderItems", "orderItems.product", "orderItems.seller"})
    Page<Order> findByCustomer(User customer, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "orderItems", "orderItems.product", "orderItems.seller"})
    Page<Order> findByCustomerAndStatus(User customer, OrderStatus status, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT o FROM Order o
            JOIN FETCH o.customer
            JOIN FETCH o.orderItems oi
            JOIN FETCH oi.product
            JOIN FETCH oi.seller
            WHERE oi.seller = :seller
            """,
            countQuery = "SELECT COUNT(DISTINCT o) FROM Order o JOIN o.orderItems oi WHERE oi.seller = :seller")
    Page<Order> findOrdersForSeller(@Param("seller") User seller, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"customer", "orderItems", "orderItems.product", "orderItems.seller"})
    Page<Order> findAll(Pageable pageable);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal getTotalRevenue();
}
