package com.hnh6625.cinema.cinema.service;

import com.hnh6625.cinema.cinema.dto.CinemaRequest;
import com.hnh6625.cinema.cinema.dto.CinemaResponse;
import com.hnh6625.cinema.cinema.entity.Cinema;
import com.hnh6625.cinema.cinema.exception.CinemaNotFoundException;
import com.hnh6625.cinema.cinema.repository.CinemaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CinemaService {
    private final CinemaRepository cinemaRepository;

    public CinemaService(CinemaRepository cinemaRepository) {
        this.cinemaRepository = cinemaRepository;
    }

    public CinemaResponse create(CinemaRequest request) {
        Cinema cinema = new Cinema(request.name().trim(), request.address().trim());
        cinemaRepository.save(cinema);
        return CinemaResponse.from(cinema);
    }

    public CinemaResponse update(Long id, CinemaRequest request) {
        Cinema cinema = findCinema(id);
        cinema.update(request.name().trim(), request.address().trim());
        return CinemaResponse.from(cinema);
    }

    public void delete(Long id) {
        Cinema cinema = findCinema(id);
        cinemaRepository.delete(cinema);
    }

    public Cinema findCinema(Long id ) {
        return cinemaRepository.findById(id).orElseThrow(() -> new CinemaNotFoundException(id));
    }
}
