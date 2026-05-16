package com.birmarket.controller;

import com.birmarket.dto.ApiResponse;
import com.birmarket.dto.ProductRequest;
import com.birmarket.dto.ProductResponse;
import com.birmarket.service.interfaces.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> browseProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 12, sort = "createdAt") Pageable pageable) {

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("minPrice cannot be greater than maxPrice"));
        }

        Page<ProductResponse> products = productService.searchProducts(categoryId, minPrice, maxPrice, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getProduct(id)));
    }

    @GetMapping("/my-products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getMyProducts(
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getMyProducts(pageable)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest req) {
        ProductResponse created = productService.createProduct(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Product listed", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Product updated", productService.updateProduct(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok("Product removed"));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateStock(
            @PathVariable Long id,
            @RequestParam @Min(value = 0, message = "Stock cant be negative") int quantity) {
        return ResponseEntity.ok(ApiResponse.ok("Stock updated", productService.updateStock(id, quantity)));
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok("Image uploaded", productService.uploadProductImage(id, file)));
    }
}
