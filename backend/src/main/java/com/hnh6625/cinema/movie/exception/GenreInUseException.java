package com.hnh6625.cinema.movie.exception;

import com.hnh6625.cinema.common.exception.ConflictException;

public class GenreInUseException extends ConflictException {
    public GenreInUseException() {
        super("Genre name is already ready");
    }
}
