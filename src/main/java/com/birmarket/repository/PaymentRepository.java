package com.birmarket.repository;

import com.birmarket.entity.Order;
import com.birmarket.entity.Payment;
import com.birmarket.entity.PaymentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = "order")
    Optional<Payment> findByOrder(Order order);

    Optional<Payment> findByConversationId(String conversationId);

    boolean existsByOrderAndPaymentStatus(Order order, PaymentStatus paymentStatus);
}
