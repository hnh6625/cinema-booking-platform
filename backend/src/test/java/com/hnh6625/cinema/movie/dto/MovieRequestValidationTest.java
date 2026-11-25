package com.hnh6625.cinema.movie.dto;

import com.hnh6625.cinema.movie.entity.AgeRating;
import com.hnh6625.cinema.movie.entity.MovieStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class MovieRequestValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private MovieRequest request(String title, Integer duration, AgeRating ageRating,
                                 MovieStatus status, Set<Long> genreIds) {
        return new MovieRequest(title, "desc", duration, ageRating, status, genreIds);
    }

    private MovieRequest validRequest() {
        return request("Avengers", 120, AgeRating.T13, MovieStatus.NOW_SHOWING, Set.of(1L));
    }

    private Set<String> violatedFields(MovieRequest request) {
        return validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(violatedFields(validRequest())).isEmpty();
    }

    @Test
    void blankTitle_isRejected() {
        MovieRequest request = request("   ", 120, AgeRating.T13, MovieStatus.NOW_SHOWING, Set.of(1L));
        assertThat(violatedFields(request)).containsExactly("title");
    }

    @Test
    void tooLongTitle_isRejected() {
        MovieRequest request = request("a".repeat(256), 120, AgeRating.T13, MovieStatus.NOW_SHOWING, Set.of(1L));
        assertThat(violatedFields(request)).containsExactly("title");
    }

    @Test
    void zeroDuration_isRejected() {
        MovieRequest request = request("Avengers", 0, AgeRating.T13, MovieStatus.NOW_SHOWING, Set.of(1L));
        assertThat(violatedFields(request)).containsExactly("durationMinutes");
    }

    @Test
    void missingDuration_isRejected() {
        MovieRequest request = request("Avengers", null, AgeRating.T13, MovieStatus.NOW_SHOWING, Set.of(1L));
        assertThat(violatedFields(request)).containsExactly("durationMinutes");
    }

    @Test
    void missingAgeRating_isRejected() {
        MovieRequest request = request("Avengers", 120, null, MovieStatus.NOW_SHOWING, Set.of(1L));
        assertThat(violatedFields(request)).containsExactly("ageRating");
    }

    @Test
    void missingStatus_isRejected() {
        MovieRequest request = request("Avengers", 120, AgeRating.T13, null, Set.of(1L));
        assertThat(violatedFields(request)).containsExactly("status");
    }

    @Test
    void emptyGenreIds_isRejected() {
        MovieRequest request = request("Avengers", 120, AgeRating.T13, MovieStatus.NOW_SHOWING, Set.of());
        assertThat(violatedFields(request)).containsExactly("genreIds");
    }

    @Test
    void moreThanFiveGenres_isRejected() {
        MovieRequest request = request("Avengers", 120, AgeRating.T13, MovieStatus.NOW_SHOWING,
                Set.of(1L, 2L, 3L, 4L, 5L, 6L));
        assertThat(violatedFields(request)).containsExactly("genreIds");
    }

    @Test
    void exactlyFiveGenres_isAccepted() {
        MovieRequest request = request("Avengers", 120, AgeRating.T13, MovieStatus.NOW_SHOWING,
                Set.of(1L, 2L, 3L, 4L, 5L));
        assertThat(violatedFields(request)).isEmpty();
    }
}