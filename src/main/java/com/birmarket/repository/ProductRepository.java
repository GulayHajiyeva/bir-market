package com.birmarket.repository;

import com.birmarket.entity.Product;
import com.birmarket.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category", "seller"})
    Optional<Product> findById(Long id);

    @EntityGraph(attributePaths = {"category", "seller"})
    @Query("""
SELECT p FROM Product p
WHERE p.active = true
AND (:categoryId IS NULL OR p.category.id = :categoryId)
AND (:minPrice IS NULL OR p.price >= :minPrice)
AND (:maxPrice IS NULL OR p.price <= :maxPrice)
AND (
    COALESCE(:keyword, '') = ''
    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
)
""")
    Page<Product> searchProducts(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword,
            Pageable pageable);

    @EntityGraph(attributePaths = {"category", "seller"})
    Page<Product> findBySellerAndActiveTrue(User seller, Pageable pageable);

    long countByActiveTrueAndStockQuantity(int stockQuantity);

    long countByActiveTrue();

    long countBySeller(User seller);

    @EntityGraph(attributePaths = {"category", "seller"})
    Optional<Product> findByIdAndSeller(Long id, User seller);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
