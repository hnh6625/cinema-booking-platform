package com.hnh6625.cinema.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

public class SecurityConfigTest {

    private final PasswordEncoder encoder = new SecurityConfig().passwordEncoder();

    @Test
    void passwordEncoder_encodeDoesNotReturnRawPassword() {
        String rawPassword = "password123";

        String hash = encoder.encode(rawPassword);

        assertThat(hash).isNotEqualTo(rawPassword);
        assertThat(hash).startsWith("$2");
    }

    @Test
    void passwordEncoder_matches_acceptsRightPasswordAndRejectsWrongOne() {
        String rawPassword = "password123";
        String hash = encoder.encode(rawPassword);

        assertThat(encoder.matches(rawPassword, hash)).isTrue();
        assertThat(encoder.matches("wrong",hash)).isFalse();
    }

    @Test
    void passwordEncoder_encodeTwice_returnsDifferentHashes() {
        String rawPassword = "password123";
        String hash1 = encoder.encode(rawPassword);
        String hash2 = encoder.encode(rawPassword);

        assertThat(hash1).isNotEqualTo(hash2);
    }
}
