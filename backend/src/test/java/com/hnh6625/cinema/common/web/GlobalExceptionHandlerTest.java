package com.hnh6625.cinema.common.web;


import com.hnh6625.cinema.common.exception.ConflictException;
import com.hnh6625.cinema.common.exception.InvalidRequestException;
import com.hnh6625.cinema.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalExceptionHandlerTest {
    @Test
    void handleConflict_returns409WithMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ConflictException exception = new ConflictException("duplicate");

        ProblemDetail result = handler.handleConflict(exception);

        assertThat(result.getStatus()).isEqualTo(409);
        assertThat(result.getDetail()).isEqualTo("duplicate");
    }

    @Test
    void handleInvalidRequest_returns400WithMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        InvalidRequestException exception = new InvalidRequestException("message");

        ProblemDetail result = handler.handleInvalidRequest(exception);

        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getDetail()).isEqualTo("message");
    }

    @Test
    void handleUnauthorized_returns401WithMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        UnauthorizedException exception = new UnauthorizedException("message");

        ProblemDetail result = handler.handleUnauthorized(exception);

        assertThat(result.getStatus()).isEqualTo(401);
        assertThat(result.getDetail()).isEqualTo("message");

    }

    @Test
    void handleNotFound_returns404WithMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        NotFoundException exception = new NotFoundException("message");

        ProblemDetail result = handler.handleNotFound(exception);

        assertThat(result.getStatus()).isEqualTo(404);
        assertThat(result.getDetail()).isEqualTo("message");
    }
}
