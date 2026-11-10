package com.hnh6625.cinema.movie.controller;

import com.hnh6625.cinema.movie.dto.GenreRequest;
import com.hnh6625.cinema.movie.dto.GenreResponse;
import com.hnh6625.cinema.movie.service.GenreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/genres")
public class GenreController {
    private final GenreService service;

    public GenreController(GenreService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public GenreResponse create(@Valid
                                @RequestBody
                                GenreRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<GenreResponse> findALl() {
        return service.findAll();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public GenreResponse rename(@PathVariable Long id,
                                @Valid
                                @RequestBody
                                GenreRequest request) {
        return service.rename(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
