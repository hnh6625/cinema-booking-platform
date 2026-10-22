package com.hnh6625.cinema.auth.exception;

import com.hnh6625.cinema.common.exception.ConflictException;

public class EmailAlreadyUsedException extends ConflictException {
    public EmailAlreadyUsedException(String message) {
        super(message);
    }
}
