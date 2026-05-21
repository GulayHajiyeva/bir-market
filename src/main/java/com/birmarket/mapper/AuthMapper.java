package com.birmarket.mapper;

import com.birmarket.dto.AuthResponse;
import com.birmarket.dto.RegisterRequest;
import com.birmarket.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "password", ignore = true)
    User toUser(RegisterRequest request);

    @Mapping(target = "token", source = "token")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "role", source = "user.role")
    AuthResponse toAuthResponse(String token, User user);
}