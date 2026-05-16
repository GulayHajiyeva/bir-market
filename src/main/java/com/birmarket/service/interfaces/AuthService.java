package com.birmarket.service.interfaces;

import com.birmarket.dto.AuthResponse;
import com.birmarket.dto.LoginRequest;
import com.birmarket.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest req);
    AuthResponse login(LoginRequest req);
}
