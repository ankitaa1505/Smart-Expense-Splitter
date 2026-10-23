package com.expensetracker.service;

import com.expensetracker.dto.request.LoginRequest;
import com.expensetracker.dto.request.RegisterRequest;
import com.expensetracker.dto.response.AuthResponse;
import com.expensetracker.dto.response.UserProfileResponse;
import com.expensetracker.entity.User;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.DuplicateResourceException;
import com.expensetracker.mapper.UserMapper;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.CustomUserDetails;
import com.expensetracker.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        log.info("Registering new user account with email: {}", normalizedEmail);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException(
                    String.format("An account with email '%s' already exists", normalizedEmail));
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = userMapper.toEntity(request, encodedPassword);
        User savedUser = userRepository.save(user);

        log.info("User registered successfully with ID: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        log.info("Authenticating user with email: {}", normalizedEmail);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String jwt = tokenProvider.generateToken(authentication);

            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new BadRequestException("User profile not found"));

            UserProfileResponse userProfile = userMapper.toDto(user);
            long expiresInSeconds = tokenProvider.getExpirationMs() / 1000;

            log.info("User '{}' authenticated successfully. JWT token issued.", normalizedEmail);
            return AuthResponse.of(jwt, expiresInSeconds, userProfile);

        } catch (BadCredentialsException ex) {
            log.warn("Authentication failed for email '{}': Invalid credentials", normalizedEmail);
            throw new BadRequestException("Invalid email or password");
        }
    }
}
