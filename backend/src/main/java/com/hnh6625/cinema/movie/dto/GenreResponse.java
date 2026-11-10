package com.hnh6625.cinema.movie.dto;

import com.hnh6625.cinema.movie.entity.Genre;

public record GenreResponse(
        Long id,
        String name
) {
    public static GenreResponse from(Genre genre) {
        return new GenreResponse(genre.getId(), genre.getName());
    }
}
