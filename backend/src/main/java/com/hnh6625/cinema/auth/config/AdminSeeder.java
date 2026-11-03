package com.hnh6625.cinema.auth.config;

import com.hnh6625.cinema.auth.entity.Role;
import com.hnh6625.cinema.auth.entity.User;
import com.hnh6625.cinema.auth.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

public class AdminSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final AdminProperties properties;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder, Clock clock, AdminProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.properties = properties;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        seed();
    }

    public void seed() {
        if(isMissing(properties.email()) || isMissing(properties.phone()) || isMissing(properties.password())) {
            return;
        }
        if(userRepository.findByEmailIgnoreCase(properties.email()).isPresent()) {
            return;
        }
        userRepository.save(new User(properties.email(), properties.phone(), passwordEncoder.encode(properties.password()), Role.ADMIN, clock.instant()));
    }

    private boolean isMissing(String value) {
        return value == null || value.isBlank();
    }
}
