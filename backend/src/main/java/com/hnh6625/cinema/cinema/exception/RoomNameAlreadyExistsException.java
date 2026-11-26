package com.hnh6625.cinema.cinema.exception;

import com.hnh6625.cinema.common.exception.ConflictException;

public class RoomNameAlreadyExistsException extends ConflictException {
    public RoomNameAlreadyExistsException(String name) {
        super("A room named '%s' already exists in this cinema.".formatted(name));
    }
}
