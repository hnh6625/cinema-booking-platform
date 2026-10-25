package com.hnh6625.cinema.auth.controller;

import com.hnh6625.cinema.auth.dto.RegisterRequest;
import com.hnh6625.cinema.auth.dto.RegisterResponse;
import com.hnh6625.cinema.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid
                                     @RequestBody
                                     RegisterRequest request) {
        return authService.register(request);
    }
}
