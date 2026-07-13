package com.splashcan.backend.security;

import com.splashcan.backend.user.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "test-secret-key-that-is-at-least-32-bytes-long!!",
            15,
            7
    );

    @Test
    void generatesAccessTokenWithExpectedClaims() {
        User user = User.builder().id(42L).email("test@splashcan.com").role(User.Role.ADMIN).build();

        String token = jwtService.generateAccessToken(user);
        Claims claims = jwtService.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("test@splashcan.com");
        assertThat(claims.get("uid", Long.class)).isEqualTo(42L);
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("type", String.class)).isEqualTo("access");
        assertThat(jwtService.isTokenValid(token, "access")).isTrue();
        assertThat(jwtService.isTokenValid(token, "refresh")).isFalse();
    }

    @Test
    void rejectsTamperedToken() {
        User user = User.builder().id(1L).email("x@y.com").role(User.Role.CUSTOMER).build();
        String token = jwtService.generateAccessToken(user);
        String tampered = token.substring(0, token.length() - 2) + "ab";

        assertThat(jwtService.isTokenValid(tampered, "access")).isFalse();
    }
}
