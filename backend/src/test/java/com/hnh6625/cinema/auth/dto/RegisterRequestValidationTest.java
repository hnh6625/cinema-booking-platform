package com.hnh6625.cinema.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class RegisterRequestValidationTest {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    // Helper method
    private Set<ConstraintViolation<RegisterRequest>> validate(
            String email, String phone, String password) {

        RegisterRequest request =
                new RegisterRequest(email, phone, password);

        return validator.validate(request);
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(validate("a@x.com", "0901234567", "password123"))
                .isEmpty();
    }

    @Test
    void password_whenShorterThan8_isRejected() {
        assertThat(validate("a@x.com", "0901234567", "short"))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("password");
    }

    @Test
    void password_whenLongerThan72_isRejected() {
        assertThat(validate("a@x.com", "0901234567", "a".repeat(73)))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("password");
    }

    @Test
    void email_whenMalformed_isRejected() {
        assertThat(validate("not-an-email", "0901234567", "password123"))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("email");
    }

    @Test
    void phone_whenBlank_isRejected() {
        assertThat(validate("a@x.com", "", "password123"))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("phone");
    }

    @Test
    void toString_doesNotContainPassword() {
        String password = "secret-password";

        RegisterRequest request =
                new RegisterRequest("a@x.com", "0901234567", password);

        assertThat(request.toString())
                .doesNotContain(password);
    }
}
