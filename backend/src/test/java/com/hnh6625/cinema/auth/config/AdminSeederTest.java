package com.hnh6625.cinema.auth.config;

import com.hnh6625.cinema.AbstractIntegrationTest;
import com.hnh6625.cinema.auth.entity.Role;
import com.hnh6625.cinema.auth.entity.User;
import com.hnh6625.cinema.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class AdminSeederTest extends AbstractIntegrationTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private Clock clock;

    private AdminSeeder seederWith(AdminProperties properties) {
        return new AdminSeeder(userRepository, passwordEncoder, clock, properties);
    }

    @Test
    void seed_whenAdminMissing_createsAdminWithHashedPassword() {
        AdminProperties properties = new AdminProperties("admin@x.com", "+84902222222", "admin-password");
        seederWith(properties).seed();

        User admin = userRepository.findByEmailIgnoreCase("admin@x.com").orElseThrow();
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(admin.getPasswordHash()).isNotEqualTo("admin-password");
        assertThat(passwordEncoder.matches("admin-password", admin.getPasswordHash())).isTrue();
    }

    @Test
    void seed_whenCalledTwice_createsOnlyOneAdmin() {
        AdminProperties properties = new AdminProperties("admin@x.com", "+84902222222", "admin-password");
        long before = userRepository.count();
        seederWith(properties).seed();
        seederWith(properties).seed();

        assertThat(userRepository.count()).isEqualTo(before + 1);

    }

    @Test
    void seed_whenEmailNotConfigured_doesNothing() {
        long before = userRepository.count();
        AdminProperties properties = new AdminProperties("", "", "");
        seederWith(properties).seed();

        assertThat(userRepository.count()).isEqualTo(before);
    }
}
