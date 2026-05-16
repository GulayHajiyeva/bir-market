package com.birmarket.dto;

import com.birmarket.entity.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {

    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private Role role;

    public AuthResponse(String token, Long userId, String email, String fullName, Role role) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }
}
