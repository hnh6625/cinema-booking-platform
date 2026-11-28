package com.hnh6625.cinema.cinema.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class CinemaRequestValidationTest {
    private static final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> violatedProperties(CinemaRequest request) {
        return validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(violatedProperties(new CinemaRequest("CGV", "Vo Van Van"))).isEmpty();
    }

    @Test
    void blankName_isRejected() {
        assertThat(violatedProperties(new CinemaRequest("   ", "Vo Van Van")))
                .containsExactly("name");
    }

    @Test
    void blankAddress_isRejected() {
        assertThat(violatedProperties(new CinemaRequest("CGV", "   ")))
                .containsExactly("address");
    }

    @Test
    void nameOfMaxLength_isAccepted_butOneMoreIsRejected() {
        assertThat(violatedProperties(new CinemaRequest("a".repeat(255), "Vo Van Van"))).isEmpty();
        assertThat(violatedProperties(new CinemaRequest("a".repeat(256), "Vo Van Van")))
                .containsExactly("name");
    }

    @Test
    void addressOfMaxLength_isAccepted_butOneMoreIsRejected() {
        assertThat(violatedProperties(new CinemaRequest("CGV", "a".repeat(500)))).isEmpty();
        assertThat(violatedProperties(new CinemaRequest("CGV", "a".repeat(501))))
                .containsExactly("address");
    }
}
