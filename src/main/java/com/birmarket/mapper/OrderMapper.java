package com.birmarket.mapper;

import com.birmarket.dto.OrderResponse;
import com.birmarket.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderResponse toResponse(Order order);
}