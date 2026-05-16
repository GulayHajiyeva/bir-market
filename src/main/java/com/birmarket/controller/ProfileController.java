package com.birmarket.controller;

import com.birmarket.dto.ApiResponse;
import com.birmarket.dto.UserResponse;
import com.birmarket.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getMyProfile()));
    }
}
