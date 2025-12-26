package com.glotrush.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_USER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String TEST_ROLE = "USER";
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900000L); // 15 minutes
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L); // 7 days
    }

    @Test
    @DisplayName("Should generate access token with role")
    void testGenerateAccessToken() {
        String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_ROLE);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        String extractedUserId = jwtService.extractUserId(token);
        String extractedRole = jwtService.extractRole(token);

        assertThat(extractedUserId).isEqualTo(TEST_USER_ID);
        assertThat(extractedRole).isEqualTo(TEST_ROLE);
    }

    @Test
    @DisplayName("Should generate refresh token without role")
    void testGenerateRefreshToken() {
        String token = jwtService.generateRefreshToken(TEST_USER_ID);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        String extractedUserId = jwtService.extractUserId(token);
        assertThat(extractedUserId).isEqualTo(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should extract user ID from token")
    void testExtractUserId() {
        String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_ROLE);
        String extractedUserId = jwtService.extractUserId(token);

        assertThat(extractedUserId).isEqualTo(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should extract role from token")
    void testExtractRole() {
        String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_ROLE);
        String extractedRole = jwtService.extractRole(token);

        assertThat(extractedRole).isEqualTo(TEST_ROLE);
    }

    @Test
    @DisplayName("Should extract username from token")
    void testExtractUsername() {
        String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_ROLE);
        String extractedUsername = jwtService.extractUsername(token);

        assertThat(extractedUsername).isEqualTo(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void testExtractExpiration() {
        String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_ROLE);
        Date expiration = jwtService.extractExpiration(token);

        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("Should validate token successfully")
    void testIsTokenValid() {
        String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_ROLE);
        boolean isValid = jwtService.isTokenValid(token, TEST_USER_ID);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should invalidate token with wrong user ID")
    void testIsTokenValidWithWrongUserId() {
        String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_ROLE);
        boolean isValid = jwtService.isTokenValid(token, "different-user-id");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should detect non-expired token")
    void testIsTokenExpired() {
        String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_ROLE);
        boolean isExpired = jwtService.isTokenExpired(token);

        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Should detect expired token")
    void testIsTokenExpiredWithExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1000L);
        String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_ROLE);

        boolean isExpired = jwtService.isTokenExpired(token);

        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("Should throw exception for invalid token signature")
    void testInvalidTokenSignature() {
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        assertThatThrownBy(() -> jwtService.extractUserId(invalidToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("Should throw exception for malformed token")
    void testMalformedToken() {
        String malformedToken = "this.is.not.a.valid.jwt.token";

        assertThatThrownBy(() -> jwtService.extractUserId(malformedToken))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    @DisplayName("Should throw exception for expired token when extracting claims")
    void testExpiredTokenExtraction() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 1L);
        String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_ROLE);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThatThrownBy(() -> jwtService.extractUserId(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Should throw exception for empty token")
    void testEmptyToken() {
        assertThatThrownBy(() -> jwtService.extractUserId(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void testGenerateDifferentTokensForDifferentUsers() {
        String token1 = jwtService.generateAccessToken("user-1", TEST_ROLE);
        String token2 = jwtService.generateAccessToken("user-2", TEST_ROLE);

        assertThat(token1).isNotEqualTo(token2);

        String userId1 = jwtService.extractUserId(token1);
        String userId2 = jwtService.extractUserId(token2);

        assertThat(userId1).isEqualTo("user-1");
        assertThat(userId2).isEqualTo("user-2");
    }

    @Test
    @DisplayName("Should generate different tokens for different roles")
    void testGenerateDifferentTokensForDifferentRoles() {
        String adminToken = jwtService.generateAccessToken(TEST_USER_ID, "ADMIN");
        String userToken = jwtService.generateAccessToken(TEST_USER_ID, "USER");

        assertThat(adminToken).isNotEqualTo(userToken);

        String adminRole = jwtService.extractRole(adminToken);
        String userRole = jwtService.extractRole(userToken);

        assertThat(adminRole).isEqualTo("ADMIN");
        assertThat(userRole).isEqualTo("USER");
    }

    @Test
    @DisplayName("Should have longer expiration for refresh token than access token")
    void testRefreshTokenLongerExpiration() {
        String accessToken = jwtService.generateAccessToken(TEST_USER_ID, TEST_ROLE);
        String refreshToken = jwtService.generateRefreshToken(TEST_USER_ID);

        Date accessExpiration = jwtService.extractExpiration(accessToken);
        Date refreshExpiration = jwtService.extractExpiration(refreshToken);

        assertThat(refreshExpiration).isAfter(accessExpiration);
    }
}
