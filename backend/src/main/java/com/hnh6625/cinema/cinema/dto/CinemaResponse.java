package com.hnh6625.cinema.cinema.dto;

import com.hnh6625.cinema.cinema.entity.Cinema;

public record CinemaResponse(Long id, String name, String address) {
    public static CinemaResponse from(Cinema cinema) {
        return new CinemaResponse(cinema.getId(), cinema.getName(), cinema.getAddress());
    }
}