package com.hnh6625.cinema.movie.dto;

import com.hnh6625.cinema.movie.entity.AgeRating;
import com.hnh6625.cinema.movie.entity.Movie;
import com.hnh6625.cinema.movie.entity.MovieStatus;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public record MovieResponse(
        Long id,
        String title,
        String description,
        int durationMinutes,
        AgeRating ageRating,
        MovieStatus status,
        List<GenreResponse> genres,
        Instant createdAt,
        Instant updatedAt
) {
    public static MovieResponse from(Movie movie) {
        List<GenreResponse> genres = movie.getGenres().stream()
                .map(GenreResponse::from)
                .sorted(Comparator.comparing(GenreResponse::name))
                .toList();
        return new MovieResponse(
                movie.getId(), movie.getTitle(), movie.getDescription(),
                movie.getDurationMinutes(), movie.getAgeRating(), movie.getStatus(),
                genres, movie.getCreatedAt(), movie.getUpdatedAt());
    }
}
