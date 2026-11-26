package com.hnh6625.cinema.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoomRenameRequest(
        @NotBlank @Size(max = 100)
        String name
) {
}
