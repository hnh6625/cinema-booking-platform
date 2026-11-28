package com.hnh6625.cinema.cinema.exception;

import com.hnh6625.cinema.common.exception.NotFoundException;

public class CinemaNotFoundException extends NotFoundException {
    public CinemaNotFoundException(Long id) {
        super("Cinema %d was not found.".formatted(id));
    }
}
