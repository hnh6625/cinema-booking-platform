package com.hnh6625.cinema.auth.exception;

import com.hnh6625.cinema.common.exception.ConflictException;

public class PhoneAlreadyUsedException extends ConflictException {
    public PhoneAlreadyUsedException(String message) {
        super(message);
    }
}
