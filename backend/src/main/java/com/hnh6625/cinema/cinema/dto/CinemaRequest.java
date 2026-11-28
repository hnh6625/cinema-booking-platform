package com.hnh6625.cinema.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CinemaRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 500) String address
) {
}
