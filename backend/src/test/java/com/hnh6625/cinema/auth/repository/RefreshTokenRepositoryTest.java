package com.hnh6625.cinema.auth.repository;

import com.hnh6625.cinema.AbstractIntegrationTest;
import com.hnh6625.cinema.auth.entity.RefreshToken;
import com.hnh6625.cinema.auth.entity.Role;
import com.hnh6625.cinema.auth.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class RefreshTokenRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void save_andFindById_returnsToken() {
        User user = userRepository.save(
                new User("a@x.com", "+84901234567", "hash", Role.USER, Instant.now()));
        Instant createdAt = Instant.parse("2026-10-28T05:00:00Z");
        Instant expiresAt = createdAt.plus(Duration.ofDays(7));
        RefreshToken token = new RefreshToken(user, "a".repeat(64), expiresAt, createdAt);

        RefreshToken saved = refreshTokenRepository.save(token);
        entityManager.flush();
        entityManager.clear();
        Optional<RefreshToken> found = refreshTokenRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTokenHash()).isEqualTo("a".repeat(64));
        assertThat(found.get().getExpiresAt()).isEqualTo(expiresAt);
        assertThat(found.get().getCreatedAt()).isEqualTo(createdAt);
        assertThat(found.get().getUsedAt()).isNull();
        assertThat(found.get().getRevokedAt()).isNull();
    }

    @Test
    void save_whenTokenHashDuplicated_throws() {
        User user = userRepository.save(
                new User("a@x.com", "+84901234567", "hash", Role.USER, Instant.now()));
        Instant createdAt = Instant.parse("2026-10-28T05:00:00Z");
        Instant expiresAt = createdAt.plus(Duration.ofDays(7));
        String sameHash = "b".repeat(64);
        RefreshToken tokenA = new RefreshToken(user, sameHash, expiresAt, createdAt);
        RefreshToken tokenB = new RefreshToken(user, sameHash, expiresAt, createdAt);

        refreshTokenRepository.saveAndFlush(tokenA);

        assertThatThrownBy(() -> refreshTokenRepository.saveAndFlush(tokenB))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}