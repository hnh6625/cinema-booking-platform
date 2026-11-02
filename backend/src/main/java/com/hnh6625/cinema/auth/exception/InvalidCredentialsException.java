package com.hnh6625.cinema.auth.exception;

import com.hnh6625.cinema.common.web.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
