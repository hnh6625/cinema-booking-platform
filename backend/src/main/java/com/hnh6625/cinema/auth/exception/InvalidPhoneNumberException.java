package com.hnh6625.cinema.auth.exception;

import com.hnh6625.cinema.common.exception.InvalidRequestException;

public class InvalidPhoneNumberException extends InvalidRequestException {
    public InvalidPhoneNumberException(String message) {
        super(message);
    }
}
