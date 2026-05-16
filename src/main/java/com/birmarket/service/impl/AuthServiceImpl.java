package com.birmarket.service.impl;

import com.birmarket.dto.AuthResponse;
import com.birmarket.dto.LoginRequest;
import com.birmarket.dto.RegisterRequest;
import com.birmarket.entity.Role;
import com.birmarket.entity.User;
import com.birmarket.exception.AlreadyExistsException;
import com.birmarket.exception.BadRequestException;
import com.birmarket.repository.UserRepository;
import com.birmarket.security.JwtUtil;
import com.birmarket.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;

    @Override
    public AuthResponse register(RegisterRequest req) {
        log.info("ActionLog.register.start");

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AlreadyExistsException("Email is already registered: " + req.getEmail());
        }

        if (req.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot register as admin");
        }

        User newUser = new User();
        newUser.setEmail(req.getEmail());
        newUser.setPassword(passwordEncoder.encode(req.getPassword()));
        newUser.setFullName(req.getFullName());
        newUser.setPhone(req.getPhone());
        newUser.setRole(req.getRole());

        User saved = userRepository.save(newUser);
        String token = jwtUtil.createToken(saved);
        AuthResponse response = new AuthResponse(token, saved.getId(), saved.getEmail(), saved.getFullName(), saved.getRole());

        log.info("ActionLog.register.end");
        return response;
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        log.info("ActionLog.login.start");

        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String token = jwtUtil.createToken(user);
        AuthResponse response = new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), user.getRole());

        log.info("ActionLog.login.end");
        return response;
    }
}
