package com.taskmanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "dGhpcy1pcy1hLXNlY3VyZS0yNTYtYml0LXNlY3JldC1rZXktZm9yLXRhc2stbWFuYWdlbWVudC1wbGF0Zm9ybQ==";
    private final long expirationMs = 3600000;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, expirationMs);
    }

    @Test
    @DisplayName("Should generate a valid JWT and successfully extract the username")
    void testGenerateAndValidateToken() {
        String token = jwtTokenProvider.generateTokenFromUsername("alice@salesforce.com", 1L, "ADMIN");

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromJwt(token)).isEqualTo("alice@salesforce.com");
    }

    @Test
    @DisplayName("Should return false when validating an invalid or tampered JWT")
    void testInvalidToken() {
        String tamperedToken = "eyJhbGciOiJIUzI1NiJ9.invalidpayload.tamperedSignature";
        assertThat(jwtTokenProvider.validateToken(tamperedToken)).isFalse();
    }
}
