package com.birmarket.dto;

import com.birmarket.enums.Role;
import com.birmarket.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private Role role;
    private boolean blocked;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User u) {
        UserResponse resp = new UserResponse();
        resp.setId(u.getId());
        resp.setEmail(u.getEmail());
        resp.setFullName(u.getFullName());
        resp.setPhone(u.getPhone());
        resp.setRole(u.getRole());
        resp.setBlocked(u.isBlocked());
        resp.setCreatedAt(u.getCreatedAt());
        return resp;
    }
}
