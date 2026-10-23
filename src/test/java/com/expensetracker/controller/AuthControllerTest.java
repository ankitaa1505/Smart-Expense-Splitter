package com.expensetracker.controller;

import com.expensetracker.dto.request.LoginRequest;
import com.expensetracker.dto.request.RegisterRequest;
import com.expensetracker.dto.response.AuthResponse;
import com.expensetracker.dto.response.UserProfileResponse;
import com.expensetracker.entity.enums.Role;
import com.expensetracker.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.expensetracker.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.expensetracker.security.CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /api/auth/register: Returns 201 Created on valid registration")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest("john@example.com", "SecurePass123!", "John", "Doe");
        UserProfileResponse response = new UserProfileResponse(
                1L, "john@example.com", "John", "Doe", Role.ROLE_USER, Instant.now()
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register: Returns 400 Bad Request when password lacks special char or digit")
    void testRegisterWeakPassword() throws Exception {
        RegisterRequest request = new RegisterRequest("john@example.com", "weakpass", "John", "Doe");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login: Returns 200 OK with Bearer token on valid credentials")
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "SecurePass123!");
        UserProfileResponse userProfile = new UserProfileResponse(
                1L, "john@example.com", "John", "Doe", Role.ROLE_USER, Instant.now()
        );
        AuthResponse response = AuthResponse.of("mock.jwt.token", 86400, userProfile);

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock.jwt.token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }
}
