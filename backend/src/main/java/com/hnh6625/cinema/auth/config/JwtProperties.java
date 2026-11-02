package com.hnh6625.cinema.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenTtl,
        Duration refreshTokenTtl
) {
    public JwtProperties {
        if (secret == null) {
            throw new IllegalArgumentException("JWT secret must not be null");
        }

        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes long");
        }


        if (accessTokenTtl == null
                || accessTokenTtl.isZero()
                || accessTokenTtl.isNegative()) {
            throw new IllegalArgumentException(
                    "JWT access token TTL must be positive"
            );
        }

        if (refreshTokenTtl == null
                || refreshTokenTtl.isZero()
                || refreshTokenTtl.isNegative()) {
            throw new IllegalArgumentException(
                    "JWT refresh token TTL must be positive"
            );
        }

    }
}
