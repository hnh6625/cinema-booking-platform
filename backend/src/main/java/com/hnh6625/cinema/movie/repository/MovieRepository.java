package com.hnh6625.cinema.movie.repository;

import com.hnh6625.cinema.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    boolean existsByGenres_Id(Long id);
}
