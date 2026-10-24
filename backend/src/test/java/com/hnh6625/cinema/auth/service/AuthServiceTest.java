package com.hnh6625.cinema.auth.service;

import com.hnh6625.cinema.auth.dto.RegisterRequest;
import com.hnh6625.cinema.auth.dto.RegisterResponse;
import com.hnh6625.cinema.auth.entity.Role;
import com.hnh6625.cinema.auth.entity.User;
import com.hnh6625.cinema.auth.exception.EmailAlreadyUsedException;
import com.hnh6625.cinema.auth.exception.InvalidPhoneNumberException;
import com.hnh6625.cinema.auth.exception.PhoneAlreadyUsedException;
import com.hnh6625.cinema.auth.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-22T05:00:00Z"), ZoneOffset.UTC);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository,passwordEncoder,clock);
    }

    @Test
    void register_withValidRequest_savesUserAndReturnsResponse() {
        RegisterRequest request = validRequest();
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("a@x.com");
        assertThat(savedUser.getPhone()).isEqualTo("+84901234567");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed");
        assertThat(savedUser.getPasswordHash())
                .isNotEqualTo("password123");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(savedUser.getCreatedAt())
                .isEqualTo(Instant.parse("2026-10-22T05:00:00Z"));
        assertThat(response.role()).isEqualTo(Role.USER);
        assertThat(response.phone()).isEqualTo("+84901234567");
    }

    @Test
    void register_whenEmailAlreadyUsed_throwsEmailAlreadyUsedException() {

        when(userRepository.existsByEmailIgnoreCase("a@x.com")).thenReturn(Boolean.TRUE);

        assertThatThrownBy(() -> authService.register(validRequest()))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepository, never()).saveAndFlush(any(User.class));
        verifyNoInteractions(passwordEncoder);

    }

    @Test
    void register_whenPhoneAlreadyUsed_throwPhoneAlreadyUsedException() {
        when(userRepository.existsByPhone("+84901234567")).thenReturn(Boolean.TRUE);

        assertThatThrownBy(() -> authService.register(validRequest()))
                .isInstanceOf(PhoneAlreadyUsedException.class);
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void register_whenPhoneInvalid_throwsInvalidPhoneNumberException() {
        RegisterRequest request = new RegisterRequest(
                "a@x.com",
                "abc",
                "password123"
        );

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(InvalidPhoneNumberException.class);

        verifyNoInteractions(passwordEncoder, userRepository);
    }

    @Test
    void register_whenEmailIndexViolated_throwsEmailAlreadyUsedException() {
        ConstraintViolationException cve = new ConstraintViolationException(
                "duplicate",
                new SQLException(),
                "ux_users_email_lower"
        );

        DataIntegrityViolationException ex = new DataIntegrityViolationException("duplicate",cve);
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(ex);

        assertThatThrownBy(() -> authService.register(validRequest()))
                .isInstanceOf(EmailAlreadyUsedException.class);
    }

    @Test
    void register_whenPhoneIndexViolated_throwsPhoneAlreadyUsedException() {
        ConstraintViolationException cve = new ConstraintViolationException(
                "duplicate",
                new SQLException(),
                "ux_users_phone"
        );
        DataIntegrityViolationException ex = new DataIntegrityViolationException("duplicate", cve);
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(ex);
        assertThatThrownBy(() -> authService.register(validRequest()))
                .isInstanceOf(PhoneAlreadyUsedException.class);

    }

    @Test
    void register_whenOtherIntegrityViolation_rethrowsOriginalError() {
        ConstraintViolationException cve = new ConstraintViolationException(
                "duplicate",
                new SQLException(),
                "some_other_constraint"
        );

        DataIntegrityViolationException ex = new DataIntegrityViolationException("duplicate", cve);
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(ex);
        assertThatThrownBy(() -> authService.register(validRequest()))
                .isSameAs(ex);
    }

    private RegisterRequest validRequest() {
        return new RegisterRequest(
                "a@x.com",
                "0901234567",
                "password123"
        );
    }

}
