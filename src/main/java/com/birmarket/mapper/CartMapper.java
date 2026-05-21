package com.birmarket.mapper;

import com.birmarket.dto.CartResponse;
import com.birmarket.entity.Cart;
import com.birmarket.entity.CartItem;
import com.birmarket.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "cartItemId", source = "id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImage", source = "product.imageUrl")
    @Mapping(target = "priceWhenAdded", source = "priceAtAddition")
    CartResponse.CartItemInfo toCartItemInfo(CartItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cart", source = "cart")
    @Mapping(target = "product", source = "product")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "priceAtAddition", source = "price")
    CartItem toCartItem(Cart cart, Product product, Integer quantity, BigDecimal price);
}