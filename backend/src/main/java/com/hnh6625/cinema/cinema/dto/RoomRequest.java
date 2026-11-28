package com.hnh6625.cinema.cinema.dto;

import com.hnh6625.cinema.cinema.entity.SeatType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record RoomRequest(
    @NotBlank
    @Size(max = 100)
    String name,
    @NotEmpty
    @Size(max = 26)
    @Valid
    List<SeatRowRequest> rows

) {
}
