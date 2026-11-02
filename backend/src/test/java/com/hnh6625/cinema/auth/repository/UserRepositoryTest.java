package com.hnh6625.cinema.auth.repository;

import com.hnh6625.cinema.AbstractIntegrationTest;
import com.hnh6625.cinema.auth.entity.Role;
import com.hnh6625.cinema.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

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

    @Test
    void findByEmailIgnoreCase_whenStoredWithDifferentCase_returnsUser() {
        repo.save(new User("a@x.com", "+84901234567", "hash", Role.USER, Instant.now()));

        Optional<User> result = repo.findByEmailIgnoreCase("A@X.COM");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("a@x.com");
    }

    @Test
    void findByPhone_whenPhoneStored_returnsUser() {
        repo.save(new User("a@x.com", "+84901234567", "hash", Role.USER, Instant.now()));

        Optional<User> result = repo.findByPhone("+84901234567");

        assertThat(result).isPresent();
        assertThat(result.get().getPhone()).isEqualTo("+84901234567");
    }

    @Test
    void findByEmailIgnoreCase_whenNotStored_returnsEmpty() {
        Optional<User> result = repo.findByEmailIgnoreCase("a@x.com");

        assertThat(result).isEmpty();
    }

    @Test
    void findByPhone_whenNotStored_returnsEmpty() {

        Optional<User> result = repo.findByPhone("+84901234567");

        assertThat(result).isEmpty();
    }
}
