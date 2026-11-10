package com.hnh6625.cinema.movie.exception;

import com.hnh6625.cinema.common.exception.NotFoundException;

public class GenreNotFoundException extends NotFoundException {
    public GenreNotFoundException(Long id) {
        super("Genre %d was not found.".formatted(id));
    }
}
