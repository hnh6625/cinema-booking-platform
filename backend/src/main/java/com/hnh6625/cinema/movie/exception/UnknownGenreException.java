package com.hnh6625.cinema.movie.exception;

import com.hnh6625.cinema.common.exception.InvalidRequestException;

public class UnknownGenreException extends InvalidRequestException {
    public UnknownGenreException() {
        super("One or more genres do not exist.");
    }
}
