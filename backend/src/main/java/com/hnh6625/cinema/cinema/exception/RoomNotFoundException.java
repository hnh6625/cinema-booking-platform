package com.hnh6625.cinema.cinema.exception;

import com.hnh6625.cinema.common.exception.NotFoundException;

public class RoomNotFoundException extends NotFoundException {
    public RoomNotFoundException(Long id) {
        super("Room %d was not found.".formatted(id));
    }
}
