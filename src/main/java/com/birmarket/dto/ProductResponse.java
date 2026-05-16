package com.birmarket.dto;

import com.birmarket.entity.Product;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private boolean inStock;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private Long sellerId;
    private String sellerName;
    private LocalDateTime createdAt;

    public static ProductResponse fromEntity(Product p) {
        ProductResponse resp = new ProductResponse();
        resp.setId(p.getId());
        resp.setName(p.getName());
        resp.setDescription(p.getDescription());
        resp.setPrice(p.getPrice());
        resp.setStockQuantity(p.getStockQuantity());
        resp.setInStock(p.getStockQuantity() > 0);
        resp.setImageUrl(p.getImageUrl());
        resp.setCategoryId(p.getCategory().getId());
        resp.setCategoryName(p.getCategory().getName());
        resp.setSellerId(p.getSeller().getId());
        resp.setSellerName(p.getSeller().getFullName());
        resp.setCreatedAt(p.getCreatedAt());
        return resp;
    }
}
