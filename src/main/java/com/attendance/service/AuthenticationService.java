package com.attendance.service;

import com.attendance.dto.LoginRequest;
import com.attendance.dto.LoginResponse;
import com.attendance.dto.UserDTO;
import com.attendance.exception.AuthenticationException;
import com.attendance.exception.DuplicateEntityException;
import com.attendance.model.User;
import com.attendance.repository.UserRepository;
import com.attendance.util.JwtTokenProvider;
import com.attendance.util.PasswordEncoderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service - handles user login/logout
 */
@Slf4j
@Service
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoderUtil passwordEncoderUtil;
    
    public AuthenticationService(UserRepository userRepository, 
                               JwtTokenProvider jwtTokenProvider,
                               PasswordEncoderUtil passwordEncoderUtil) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoderUtil = passwordEncoderUtil;
    }
    
    /**
     * Login user and return JWT tokens
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new AuthenticationException("Invalid email or password"));
        
        if (!user.getIsActive()) {
            throw new AuthenticationException("Account is inactive");
        }
        
        if (!passwordEncoderUtil.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password");
        }
        
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        log.info("User logged in: {}", user.getEmail());
        
        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(3600L)
            .user(UserDTO.from(user))
            .build();
    }
    
    /**
     * Refresh access token
     */
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token");
        }
        
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticationException("User not found"));
        
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        
        return LoginResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(refreshToken)
            .expiresIn(3600L)
            .user(UserDTO.from(user))
            .build();
    }

    /**
     * Create application JWTs for a user authenticated by an OAuth2/OIDC provider.
     */
    @Transactional(readOnly = true)
    public LoginResponse loginWithOAuthEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticationException("OAuth account is not registered: " + email));

        if (!user.getIsActive()) {
            throw new AuthenticationException("Account is inactive");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("User logged in with OAuth: {}", user.getEmail());

        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(3600L)
            .user(UserDTO.from(user))
            .build();
    }
    
    /**
     * Register new user (teacher use only)
     */
    @Transactional
    public UserDTO registerUser(String email, String name, String phone, String password, User.UserRole role) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEntityException("Email already registered: " + email);
        }
        
        User user = User.builder()
            .email(email)
            .name(name)
            .phone(phone)
            .passwordHash(passwordEncoderUtil.encodePassword(password))
            .role(role)
            .isActive(true)
            .build();
        
        User saved = userRepository.save(user);
        log.info("New user registered: {}", email);
        return UserDTO.from(saved);
    }
}
