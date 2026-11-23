package com.hnh6625.cinema.movie.dto;

import com.hnh6625.cinema.movie.entity.AgeRating;
import com.hnh6625.cinema.movie.entity.MovieStatus;
import jakarta.validation.constraints.*;

import java.util.Set;

public record MovieRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        String description,
        @NotNull
        @Positive
        Integer durationMinutes,
        @NotNull
        AgeRating ageRating,
        @NotNull
        MovieStatus status,
        @NotEmpty
        @Size(max = 5)
        Set<Long> genreIds
) {
}
