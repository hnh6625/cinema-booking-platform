package com.hnh6625.cinema.movie.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class GenreRequestValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<String> violatedFields(GenreRequest request) {
        return validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void validName_hasNoViolations() {
        assertThat(violatedFields(new GenreRequest("Action"))).isEmpty();
    }

    @Test
    void blankName_isRejected() {
        assertThat(violatedFields(new GenreRequest("   "))).containsExactly("name");
    }

    @Test
    void nullName_isRejected() {
        assertThat(violatedFields(new GenreRequest(null))).containsExactly("name");
    }

    @Test
    void nameOf50Characters_isAccepted() {
        assertThat(violatedFields(new GenreRequest("a".repeat(50)))).isEmpty();
    }

    @Test
    void nameOf51Characters_isRejected() {
        assertThat(violatedFields(new GenreRequest("a".repeat(51)))).containsExactly("name");
    }
}