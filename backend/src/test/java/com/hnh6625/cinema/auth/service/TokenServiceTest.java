package com.hnh6625.cinema.auth.service;

import com.hnh6625.cinema.auth.config.JwtConfig;
import com.hnh6625.cinema.auth.config.JwtProperties;
import com.hnh6625.cinema.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

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

    private User createUser() {
        User user = BeanUtils.instantiateClass(User.class);

        ReflectionTestUtils.setField(user, "id", 42L);

        Field roleField = ReflectionUtils.findField(User.class, "role");
        if (roleField == null || !roleField.getType().isEnum()) {
            throw new IllegalStateException(
                    "Cannot find the role enum type for the field in User"
            );
        }

        ReflectionTestUtils.setField(
                user,
                "role",
                enumValue(roleField.getType(), "USER")
        );

        return user;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Enum<?> enumValue(Class<?> enumType, String name) {
        return Enum.valueOf((Class) enumType, name);
    }
}
