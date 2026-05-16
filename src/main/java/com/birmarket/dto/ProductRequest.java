package com.birmarket.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be more than 0")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cant be negative")
    private Integer stockQuantity = 0;

    private String imageUrl;

    @NotNull(message = "Category is required")
    private Long categoryId;
}
