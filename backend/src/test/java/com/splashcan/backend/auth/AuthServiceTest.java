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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerHashesPasswordAndIssuesTokens() {
        RegisterRequest request = new RegisterRequest("new@user.com", "password123", "New User");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        Claims claims = mock(Claims.class);
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 60_000));
        when(jwtService.parseClaims("refresh-token")).thenReturn(claims);

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(userCaptor.capture());
        User savedUser = userCaptor.getAllValues().get(0);
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getRole()).isEqualTo(User.Role.CUSTOMER);
    }

    @Test
    void registerThrowsWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("dup@user.com", "password123", "Dup User");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void loginThrowsOnWrongPassword() {
        User user = User.builder().id(1L).email("a@b.com").passwordHash("hashed").role(User.Role.CUSTOMER).build();
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("a@b.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginSucceedsAndIssuesTokens() {
        User user = User.builder().id(1L).email("a@b.com").passwordHash("hashed").role(User.Role.CUSTOMER).build();
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        Claims claims = mock(Claims.class);
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 60_000));
        when(jwtService.parseClaims("refresh-token")).thenReturn(claims);

        AuthResponse response = authService.login(new LoginRequest("a@b.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRefreshTokenHash()).isNotBlank();
        assertThat(userCaptor.getValue().getRefreshTokenExpiresAt()).isNotNull();
    }

    @Test
    void loginThrowsWhenEmailNotFound() {
        when(userRepository.findByEmail("nope@b.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nope@b.com", "whatever")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void refreshIssuesNewTokensWhenValid() {
        String presentedToken = "presented-refresh-token";
        User user = User.builder()
                .id(1L)
                .email("a@b.com")
                .role(User.Role.CUSTOMER)
                .refreshTokenHash(sha256Base64(presentedToken))
                .refreshTokenExpiresAt(OffsetDateTime.now().plusDays(1))
                .build();

        when(jwtService.isTokenValid(presentedToken, "refresh")).thenReturn(true);
        Claims presentedClaims = mock(Claims.class);
        when(presentedClaims.get("uid", Long.class)).thenReturn(1L);
        when(jwtService.parseClaims(presentedToken)).thenReturn(presentedClaims);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh-token");
        Claims newRefreshClaims = mock(Claims.class);
        when(newRefreshClaims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 60_000));
        when(jwtService.parseClaims("new-refresh-token")).thenReturn(newRefreshClaims);

        AuthResponse response = authService.refresh(new RefreshRequest(presentedToken));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        verify(userRepository).save(user);
    }

    @Test
    void refreshThrowsWhenTokenInvalid() {
        when(jwtService.isTokenValid("bad-token", "refresh")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest("bad-token")))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refreshThrowsWhenUserIdFromClaimsNotFound() {
        String token = "valid-token";
        when(jwtService.isTokenValid(token, "refresh")).thenReturn(true);
        Claims claims = mock(Claims.class);
        when(claims.get("uid", Long.class)).thenReturn(99L);
        when(jwtService.parseClaims(token)).thenReturn(claims);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest(token)))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refreshThrowsWhenStoredHashDoesNotMatchPresentedToken() {
        String token = "presented-token";
        User user = User.builder()
                .id(1L)
                .email("a@b.com")
                .role(User.Role.CUSTOMER)
                .refreshTokenHash("some-other-hash-from-a-rotated-token")
                .refreshTokenExpiresAt(OffsetDateTime.now().plusDays(1))
                .build();
        when(jwtService.isTokenValid(token, "refresh")).thenReturn(true);
        Claims claims = mock(Claims.class);
        when(claims.get("uid", Long.class)).thenReturn(1L);
        when(jwtService.parseClaims(token)).thenReturn(claims);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest(token)))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refreshThrowsWhenExpiresAtIsNull() {
        String token = "presented-token";
        User user = User.builder()
                .id(1L)
                .email("a@b.com")
                .role(User.Role.CUSTOMER)
                .refreshTokenHash(sha256Base64(token))
                .refreshTokenExpiresAt(null)
                .build();
        when(jwtService.isTokenValid(token, "refresh")).thenReturn(true);
        Claims claims = mock(Claims.class);
        when(claims.get("uid", Long.class)).thenReturn(1L);
        when(jwtService.parseClaims(token)).thenReturn(claims);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest(token)))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refreshThrowsWhenExpiresAtIsInThePast() {
        String token = "presented-token";
        User user = User.builder()
                .id(1L)
                .email("a@b.com")
                .role(User.Role.CUSTOMER)
                .refreshTokenHash(sha256Base64(token))
                .refreshTokenExpiresAt(OffsetDateTime.now().minusMinutes(1))
                .build();
        when(jwtService.isTokenValid(token, "refresh")).thenReturn(true);
        Claims claims = mock(Claims.class);
        when(claims.get("uid", Long.class)).thenReturn(1L);
        when(jwtService.parseClaims(token)).thenReturn(claims);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest(token)))
                .isInstanceOf(InvalidTokenException.class);
    }

    // Mirrors AuthService's private `hash` method (SHA-256 + Base64) so refresh-token
    // fixtures can present a stored hash that matches a given plaintext token.
    private static String sha256Base64(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(value.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
