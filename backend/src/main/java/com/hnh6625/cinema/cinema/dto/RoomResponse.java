package com.hnh6625.cinema.cinema.dto;

public record RoomResponse(
        Long id,
        Long cinemaId,
        String name,
        int seatCount
) {

}
