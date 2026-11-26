package com.hnh6625.cinema.cinema.dto;

import com.hnh6625.cinema.cinema.entity.SeatType;
import jakarta.validation.constraints.*;

public record SeatRowRequest(
        @NotBlank
        @Size(max = 3)
        String label,

        @NotNull
        @Min(1) @Max(50)
        Integer seatCount,

        @NotNull
        SeatType type
) {
}
