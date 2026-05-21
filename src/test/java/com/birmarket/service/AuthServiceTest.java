package com.birmarket.service;

import com.birmarket.mapper.AuthMapper;
import com.birmarket.service.impl.AuthServiceImpl;
import com.birmarket.dto.AuthResponse;
import com.birmarket.dto.LoginRequest;
import com.birmarket.dto.RegisterRequest;
import com.birmarket.enums.Role;
import com.birmarket.entity.User;
import com.birmarket.exception.AlreadyExistsException;
import com.birmarket.exception.BadRequestException;
import com.birmarket.repository.UserRepository;
import com.birmarket.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private AuthMapper authMapper;
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerReq;
    private User testUser;

    @BeforeEach
    void setup() {
        registerReq = new RegisterRequest();
        registerReq.setEmail("test@test.com");
        registerReq.setPassword("Test@1234");
        registerReq.setFullName("Test User");
        registerReq.setRole(Role.CUSTOMER);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@test.com");
        testUser.setFullName("Test User");
        testUser.setRole(Role.CUSTOMER);
    }

    @Test
    void register_success() {

        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);

        when(authMapper.toUser(any(RegisterRequest.class)))
                .thenReturn(testUser);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("hashedpassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        when(jwtUtil.createToken(any()))
                .thenReturn("some-token");

        when(authMapper.toAuthResponse(anyString(), any(User.class)))
                .thenReturn(
                        new AuthResponse(
                                "some-token",
                                1L,
                                "test@test.com",
                                "Test User",
                                Role.CUSTOMER
                        )
                );

        AuthResponse result = authService.register(registerReq);

        assertNotNull(result);
        assertEquals("some-token", result.getToken());
        assertEquals("test@test.com", result.getEmail());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_fails_when_email_already_exists() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        // should throw because email is taken
        assertThrows(AlreadyExistsException.class, () -> authService.register(registerReq));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_fails_when_role_is_admin() {
        registerReq.setRole(Role.ADMIN);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.register(registerReq));
    }

    @Test
    void login_success() {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("test@test.com");
        loginReq.setPassword("Test@1234");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(testUser));

        when(jwtUtil.createToken(any()))
                .thenReturn("login-token");

        when(authMapper.toAuthResponse(anyString(), any(User.class)))
                .thenReturn(
                        new AuthResponse(
                                "login-token",
                                1L,
                                "test@test.com",
                                "Test User",
                                Role.CUSTOMER
                        )
                );

        AuthResponse result = authService.login(loginReq);

        assertEquals("login-token", result.getToken());
        assertEquals(Role.CUSTOMER, result.getRole());
    }
}
