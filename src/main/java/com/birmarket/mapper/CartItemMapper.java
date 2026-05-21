package com.birmarket.mapper;

import com.birmarket.entity.Cart;
import com.birmarket.entity.CartItem;
import com.birmarket.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cart", source = "cart")
    @Mapping(target = "product", source = "product")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "priceAtAddition", source = "price")
    CartItem toCartItem(Cart cart, Product product, Integer quantity, BigDecimal price);
}
