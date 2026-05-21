package com.birmarket.mapper;

import com.birmarket.dto.UserResponse;
import com.birmarket.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}
