package com.hnh6625.cinema.auth.service;

import com.hnh6625.cinema.auth.TokenHasher;
import com.hnh6625.cinema.auth.config.JwtProperties;
import com.hnh6625.cinema.auth.entity.RefreshToken;
import com.hnh6625.cinema.auth.entity.User;
import com.hnh6625.cinema.auth.repository.RefreshTokenRepository;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;

@Service
public class TokenService {
    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;
    private final Clock clock;
    private final RefreshTokenRepository rfTokenRepository;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public TokenService(JwtEncoder jwtEncoder, JwtProperties properties, Clock clock, RefreshTokenRepository rfTokenRepository) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.clock = clock;
        this.rfTokenRepository = rfTokenRepository;
    }

    public String createAccessToken(User user) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.accessTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    public String createRefreshToken(User user) {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Instant createdAt = clock.instant();
        Instant expiresAt = createdAt.plus(properties.refreshTokenTtl());
        String tokenHash = TokenHasher.sha256Hex(raw);

        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHash,
                expiresAt,
                createdAt
        );

        rfTokenRepository.save(refreshToken);

        return raw;
    }
}
