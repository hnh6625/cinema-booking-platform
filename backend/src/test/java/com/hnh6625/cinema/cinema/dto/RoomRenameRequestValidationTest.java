package com.hnh6625.cinema.cinema.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class RoomRenameRequestValidationTest {
    private static final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> violatedProperties(RoomRenameRequest request) {
        return validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void validName_hasNoViolations() {
        assertThat(violatedProperties(new RoomRenameRequest("Room 2"))).isEmpty();
    }

    @Test
    void blankName_isRejected() {
        assertThat(violatedProperties(new RoomRenameRequest("   ")))
                .containsExactly("name");
    }

    @Test
    void nameOfMaxLength_isAccepted_butOneMoreIsRejected() {
        assertThat(violatedProperties(new RoomRenameRequest("a".repeat(100)))).isEmpty();
        assertThat(violatedProperties(new RoomRenameRequest("a".repeat(101))))
                .containsExactly("name");
    }
}