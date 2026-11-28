package com.hnh6625.cinema.cinema.controller;

import com.hnh6625.cinema.cinema.dto.CinemaRequest;
import com.hnh6625.cinema.cinema.dto.CinemaResponse;
import com.hnh6625.cinema.cinema.service.CinemaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cinemas")
public class CinemaController {
    private final CinemaService cinemaService;

    public CinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CinemaResponse create(@Valid @RequestBody CinemaRequest request) {
        return cinemaService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CinemaResponse update(@PathVariable Long id, @Valid @RequestBody CinemaRequest request) {
        return cinemaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        cinemaService.delete(id);
    }
}