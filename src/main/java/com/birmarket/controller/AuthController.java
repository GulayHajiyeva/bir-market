package com.birmarket.controller;

import com.birmarket.dto.ApiResponse;
import com.birmarket.dto.AuthResponse;
import com.birmarket.dto.LoginRequest;
import com.birmarket.dto.RegisterRequest;
import com.birmarket.service.interfaces.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse result = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Account created", result));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse result = authService.login(req);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", result));
    }
}
