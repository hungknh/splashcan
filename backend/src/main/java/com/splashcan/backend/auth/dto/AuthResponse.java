package com.splashcan.backend.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
