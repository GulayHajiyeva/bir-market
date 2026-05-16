package com.birmarket.repository;

import com.birmarket.entity.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @EntityGraph(attributePaths = "products")
    List<Category> findByActiveTrue();

    @EntityGraph(attributePaths = "products")
    List<Category> findAll();

    @EntityGraph(attributePaths = "products")
    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);

    boolean existsByName(String name);
}
