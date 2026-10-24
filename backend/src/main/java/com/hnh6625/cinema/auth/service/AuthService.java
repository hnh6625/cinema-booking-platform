package com.hnh6625.cinema.auth.service;

import com.hnh6625.cinema.auth.PhoneNumberNormalizer;
import com.hnh6625.cinema.auth.dto.RegisterRequest;
import com.hnh6625.cinema.auth.dto.RegisterResponse;
import com.hnh6625.cinema.auth.entity.Role;
import com.hnh6625.cinema.auth.entity.User;
import com.hnh6625.cinema.auth.exception.EmailAlreadyUsedException;
import com.hnh6625.cinema.auth.exception.PhoneAlreadyUsedException;
import com.hnh6625.cinema.auth.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class AuthService {
    private static final String EMAIL_UNIQUE_CONSTRAINT = "ux_users_email_lower";
    private static final String PHONE_UNIQUE_CONSTRAINT = "ux_users_phone";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizePhone = PhoneNumberNormalizer.normalize(request.phone());
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new EmailAlreadyUsedException("Email is already registered.");
        }
        if (userRepository.existsByPhone(normalizePhone)) {
            throw new PhoneAlreadyUsedException("Phone is already registered.");
        }
        String hashPassword = passwordEncoder.encode(request.password());

        User user = new User(
                request.email(),
                normalizePhone,
                hashPassword,
                Role.USER,
                clock.instant());
        try {
            User saved = userRepository.saveAndFlush(user);
            return RegisterResponse.from(saved);
        } catch (DataIntegrityViolationException ex) {
            String constraintName = findConstraintName(ex);
            if (EMAIL_UNIQUE_CONSTRAINT.equals(constraintName)) {
                throw new EmailAlreadyUsedException("Email is already registered.");
            }
            if (PHONE_UNIQUE_CONSTRAINT.equals(constraintName)) {
                throw new PhoneAlreadyUsedException("Phone is already registered.");
            }
            throw ex;
        }
    }
    private String findConstraintName(DataIntegrityViolationException ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException cve) {
                return cve.getConstraintName();
            }

            cause = cause.getCause();
        }
        return null;
    }
}
