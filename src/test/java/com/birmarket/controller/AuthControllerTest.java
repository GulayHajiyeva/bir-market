package com.birmarket.controller;

import com.birmarket.dto.AuthResponse;
import com.birmarket.dto.LoginRequest;
import com.birmarket.dto.RegisterRequest;
import com.birmarket.enums.Role;
import com.birmarket.service.interfaces.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void register_returns_201_with_token() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@test.com");
        req.setPassword("Test@1234");
        req.setFullName("New User");
        req.setPhone("+994501234567");
        req.setRole(Role.CUSTOMER);

        AuthResponse fakeResponse = new AuthResponse("fake-token", 1L, "new@test.com", "New User", Role.CUSTOMER);
        when(authService.register(any())).thenReturn(fakeResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("fake-token"))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }

    @Test
    void register_returns_400_when_email_is_invalid() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("notanemail");
        req.setPassword("Test@1234");
        req.setFullName("Test");
        req.setRole(Role.CUSTOMER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_returns_400_when_password_too_short() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("abc"); // too short
        req.setFullName("Test User");
        req.setRole(Role.CUSTOMER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returns_200_with_token() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("Test@1234");

        AuthResponse fakeResponse = new AuthResponse("login-token", 1L, "test@test.com", "Test User", Role.CUSTOMER);
        when(authService.login(any())).thenReturn(fakeResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("login-token"));
    }

    @Test
    void login_returns_400_when_body_is_empty() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
