package com.hnh6625.cinema.auth.dto;

import com.hnh6625.cinema.auth.entity.Role;
import com.hnh6625.cinema.auth.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class RegisterResponseTest {

    @Test
    void from_mapsUserFieldsToResponse() {
        Instant createdAt = Instant.parse("2026-10-22T05:00:00Z");
        User user = new User("a@x.com", "+84901234567", "hash", Role.USER, createdAt);

        RegisterResponse res = RegisterResponse.from(user);

        assertThat(res.email()).isEqualTo("a@x.com");
        assertThat(res.phone()).isEqualTo("+84901234567");
        assertThat(res.role()).isEqualTo(Role.USER);
        assertThat(res.createdAt()).isEqualTo(createdAt);
        assertThat(res.id()).isNull();
    }
}
