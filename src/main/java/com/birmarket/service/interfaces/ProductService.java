package com.birmarket.service.interfaces;

import com.birmarket.dto.ProductRequest;
import com.birmarket.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public interface ProductService {
    Page<ProductResponse> searchProducts(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String keyword, Pageable pageable);
    ProductResponse getProduct(Long id);
    ProductResponse createProduct(ProductRequest req);
    ProductResponse updateProduct(Long id, ProductRequest req);
    void deleteProduct(Long id);
    ProductResponse updateStock(Long id, int quantity);
    Page<ProductResponse> getMyProducts(Pageable pageable);
    ProductResponse uploadProductImage(Long productId, MultipartFile file);
}
