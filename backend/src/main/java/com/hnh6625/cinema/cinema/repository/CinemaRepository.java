package com.hnh6625.cinema.cinema.repository;

import com.hnh6625.cinema.cinema.entity.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {
}
