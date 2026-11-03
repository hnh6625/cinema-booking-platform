package com.hnh6625.cinema.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestSecurityController {

    @GetMapping("/api/v1/test/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin() {
        return "ok";
    }

    @GetMapping("/api/v1/test/authenticated")
    public String authenticated() {
        return "ok";
    }
}
