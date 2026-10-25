package com.hnh6625.cinema.common.web;


import com.hnh6625.cinema.common.exception.ConflictException;
import com.hnh6625.cinema.common.exception.InvalidRequestException;
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
}
