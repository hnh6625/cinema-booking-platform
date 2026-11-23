package com.hnh6625.cinema.movie.exception;

import com.hnh6625.cinema.common.exception.NotFoundException;

public class MovieNotFoundException extends NotFoundException {
    public MovieNotFoundException(Long id) {
        super("Movie %d was not found.".formatted(id));
    }
}
