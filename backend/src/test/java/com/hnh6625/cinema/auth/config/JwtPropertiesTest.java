package com.hnh6625.cinema.auth.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JwtPropertiesTest {
    @Test
    void constructor_withValidValues_succeeds() {
        String secret = "a".repeat(32);
        Duration accessTokenTtl = Duration.ofMinutes(15);
        Duration refreshTokenTtl = Duration.ofDays(7);

        JwtProperties properties = new JwtProperties(
                secret,
                accessTokenTtl,
                refreshTokenTtl
        );

        assertThat(properties.secret()).isEqualTo(secret);
        assertThat(properties.accessTokenTtl()).isEqualTo(accessTokenTtl);
        assertThat(properties.refreshTokenTtl()).isEqualTo(refreshTokenTtl);
    }

    @Test
    void constructor_whenSecretShorterThan32Bytes_throws() {
        assertThatThrownBy( () -> new JwtProperties(
                        "short",
                        Duration.ofMinutes(15),
                        Duration.ofDays(7)
                )
        ).isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void constructor_whenSecretNull_throws() {
        assertThatThrownBy( () -> new JwtProperties(
                        null,
                        Duration.ofMinutes(15),
                        Duration.ofDays(7))
        ).isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void constructor_whenAccessTtlNull_throws() {
        assertThatThrownBy(() -> new JwtProperties(
                "a".repeat(32),
                null,
                Duration.ofDays(7))
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_whenRefreshTtlNull_throws() {
        assertThatThrownBy(() -> new JwtProperties(
                "a".repeat(32),
                Duration.ofMinutes(15),
                null)
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
