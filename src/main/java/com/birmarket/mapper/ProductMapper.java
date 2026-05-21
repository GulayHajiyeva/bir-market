package com.birmarket.mapper;


import com.birmarket.dto.ProductResponse;
import com.birmarket.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductResponse toResponse(Product product);
}
