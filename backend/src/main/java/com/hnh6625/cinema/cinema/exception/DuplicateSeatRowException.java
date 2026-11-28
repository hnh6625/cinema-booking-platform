package com.hnh6625.cinema.cinema.exception;

import com.hnh6625.cinema.common.exception.InvalidRequestException;

public class DuplicateSeatRowException extends InvalidRequestException {
    public DuplicateSeatRowException(String label) {
        super("Seat row '%s' appears more than once.".formatted(label));
    }
}
