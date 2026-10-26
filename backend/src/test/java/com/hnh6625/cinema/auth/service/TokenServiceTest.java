package com.hnh6625.cinema.auth.service;

import com.hnh6625.cinema.auth.config.JwtConfig;
import com.hnh6625.cinema.auth.config.JwtProperties;
import com.hnh6625.cinema.auth.entity.Role;
import com.hnh6625.cinema.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TokenServiceTest {
    private JwtProperties properties;
    private JwtEncoder encoder;
    private JwtDecoder decoder;
    private User user;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties(
                "a".repeat(32),
                Duration.ofMinutes(15),
                Duration.ofDays(7)
        );

        JwtConfig jwtConfig = new JwtConfig();
        encoder = jwtConfig.jwtEncoder(properties);
        decoder = jwtConfig.jwtDecoder(properties);

        user = createUser();
    }

    @Test
    void createAccessToken_whenTokenTampered_isRejected() {
        Clock clock = Clock.systemUTC();
        TokenService tokenService = new TokenService(encoder, properties, clock);

        String token = tokenService.createAccessToken(user);
        String[] parts = token.split("\\.");

        char replacement = parts[2].charAt(0) == 'A' ? 'B' : 'A';
        parts[2] = replacement + parts[2].substring(1);

        String tamperedToken = String.join(".", parts);

        assertThatThrownBy(() -> decoder.decode(tamperedToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void createAccessToken_whenExpired_isRejected() {
        Clock clock = Clock.fixed(Instant.now().minus(1, ChronoUnit.HOURS),
                ZoneOffset.UTC
        );

        TokenService tokenService = new TokenService(encoder, properties, clock);

        String token = tokenService.createAccessToken(user);

        assertThatThrownBy(() -> decoder.decode(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void createAccessToken_containsSubjectRoleAndExpiry() {
        Instant now = Instant.now();
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);

        TokenService tokenService = new TokenService(encoder, properties, clock);

        String token = tokenService.createAccessToken(user);

        Jwt jwt = decoder.decode(token);

        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(jwt.getClaimAsString("role")).isEqualTo("USER");
        assertThat(jwt.getIssuedAt()).isEqualTo(now.truncatedTo(ChronoUnit.SECONDS));
        assertThat(jwt.getExpiresAt()).isEqualTo(now.plus(Duration.ofMinutes(15)).truncatedTo(ChronoUnit.SECONDS));
    }

    private User createUser() {
        User user = new User("a@x.com", "+84901234567", "hash", Role.USER, Instant.now());
        ReflectionTestUtils.setField(user, "id", 42L);
        return user;
    }
}
