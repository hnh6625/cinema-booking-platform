package com.hnh6625.cinema.auth.repository;

import com.hnh6625.cinema.AbstractIntegrationTest;
import com.hnh6625.cinema.auth.entity.Role;
import com.hnh6625.cinema.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class UserRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository repo;

    @Test
    void existsByEmailIgnoreCase_whenEmailStoredWithDifferentCase_returnsTrue() {
        repo.save(new User("a@x.com", "+84901234567", "hash", Role.USER, Instant.now()));

        boolean exists = repo.existsByEmailIgnoreCase("A@X.COM");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmailIgnoreCase_whenEmailNotStored_returnsFalse() {
        repo.save(new User("a@x.com", "+84901234567", "hash", Role.USER, Instant.now()));

        boolean exists = repo.existsByEmailIgnoreCase("b@x.com");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByPhone_whenPhoneStored_returnsTrue() {
        repo.save(new User("a@x.com", "+84901234567", "hash", Role.USER, Instant.now()));

        boolean exists = repo.existsByPhone("+84901234567");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByPhone_whenPhoneNotStored_returnsFalse() {
        repo.save(new User("a@x.com", "+84901234567", "hash", Role.USER, Instant.now()));

        boolean exists = repo.existsByPhone("+84912345678");

        assertThat(exists).isFalse();
    }
}
