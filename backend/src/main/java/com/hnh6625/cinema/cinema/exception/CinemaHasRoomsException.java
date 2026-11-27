package com.hnh6625.cinema.cinema.exception;

import com.hnh6625.cinema.common.exception.ConflictException;

public class CinemaHasRoomsException extends ConflictException {
    public CinemaHasRoomsException(Long id) {
        super("Cinema %d cannot be deleted because it still has rooms.".formatted(id));
    }
}