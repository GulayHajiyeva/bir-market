package com.birmarket.repository;

import com.birmarket.entity.Cart;
import com.birmarket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);

    @Query("""
    SELECT c
    FROM Cart c
    LEFT JOIN FETCH c.items ci
    LEFT JOIN FETCH ci.product
    WHERE c.user = :user
""")
    Optional<Cart> findByUserWithItems(User user);
}
