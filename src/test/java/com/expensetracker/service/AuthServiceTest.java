package com.expensetracker.service;

import com.expensetracker.dto.request.LoginRequest;
import com.expensetracker.dto.request.RegisterRequest;
import com.expensetracker.dto.response.AuthResponse;
import com.expensetracker.dto.response.UserProfileResponse;
import com.expensetracker.entity.User;
import com.expensetracker.entity.enums.Role;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.DuplicateResourceException;
import com.expensetracker.mapper.UserMapper;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.CustomUserDetails;
import com.expensetracker.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private UserProfileResponse profileResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("newuser@example.com", "Password123!", "Alice", "Wonder");
        loginRequest = new LoginRequest("newuser@example.com", "Password123!");

        user = new User("newuser@example.com", "encodedPassword", "Alice", "Wonder", Role.ROLE_USER);
        user.setId(5L);

        profileResponse = new UserProfileResponse(
                5L, "newuser@example.com", "Alice", "Wonder", Role.ROLE_USER, Instant.now()
        );
    }

    @Test
    @DisplayName("register: Successfully hashes password, saves user, and returns profile")
    void testRegisterSuccess() {
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userMapper.toEntity(registerRequest, "encodedPassword")).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(profileResponse);

        UserProfileResponse result = authService.register(registerRequest);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("newuser@example.com");
        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("register: Throws DuplicateResourceException when email is already registered")
    void testRegisterDuplicateEmail() {
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login: Successfully authenticates and issues JWT access token")
    void testLoginSuccess() {
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(5L);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt.token.string");
        when(tokenProvider.getExpirationMs()).thenReturn(86400000L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(profileResponse);

        AuthResponse result = authService.login(loginRequest);

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("jwt.token.string");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresIn()).isEqualTo(86400L);
        assertThat(result.user().email()).isEqualTo("newuser@example.com");
    }

    @Test
    @DisplayName("login: Throws BadRequestException when credentials are invalid")
    void testLoginInvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid email or password");
    }
}
