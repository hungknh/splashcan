package com.splashcan.backend.auth;

import com.splashcan.backend.auth.dto.AuthResponse;
import com.splashcan.backend.auth.dto.LoginRequest;
import com.splashcan.backend.auth.dto.RefreshRequest;
import com.splashcan.backend.auth.dto.RegisterRequest;
import com.splashcan.backend.exception.EmailAlreadyExistsException;
import com.splashcan.backend.exception.InvalidCredentialsException;
import com.splashcan.backend.exception.InvalidTokenException;
import com.splashcan.backend.security.JwtService;
import com.splashcan.backend.user.User;
import com.splashcan.backend.user.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(User.Role.CUSTOMER)
                .build();
        userRepository.save(user);
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();
        if (!jwtService.isTokenValid(token, "refresh")) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }
        Claims claims = jwtService.parseClaims(token);
        Long userId = claims.get("uid", Long.class);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!Objects.equals(user.getRefreshTokenHash(), hash(token))) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }
        if (user.getRefreshTokenExpiresAt() == null || user.getRefreshTokenExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new InvalidTokenException("Refresh token expired");
        }
        return issueTokens(user);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        Claims refreshClaims = jwtService.parseClaims(refreshToken);
        user.setRefreshTokenHash(hash(refreshToken));
        user.setRefreshTokenExpiresAt(refreshClaims.getExpiration().toInstant().atOffset(ZoneOffset.UTC));
        userRepository.save(user);
        return new AuthResponse(accessToken, refreshToken);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(value.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
