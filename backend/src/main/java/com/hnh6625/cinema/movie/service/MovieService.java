package com.hnh6625.cinema.movie.service;

import com.hnh6625.cinema.movie.dto.MovieRequest;
import com.hnh6625.cinema.movie.dto.MovieResponse;
import com.hnh6625.cinema.movie.entity.Genre;
import com.hnh6625.cinema.movie.entity.Movie;
import com.hnh6625.cinema.movie.exception.MovieNotFoundException;
import com.hnh6625.cinema.movie.exception.UnknownGenreException;
import com.hnh6625.cinema.movie.repository.GenreRepository;
import com.hnh6625.cinema.movie.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class MovieService {
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final Clock clock;

    public MovieService(MovieRepository movieRepository, GenreRepository genreRepository, Clock clock) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.clock = clock;
    }

    public MovieResponse create(MovieRequest request) {
        Set<Genre> genres = loadGenres(request.genreIds());
        Movie movie = new Movie(
                request.title().trim(),
                request.description(),
                request.durationMinutes(),
                request.ageRating(),
                request.status(),
                genres,
                clock.instant());
        return MovieResponse.from(movieRepository.save(movie));
    }

    public MovieResponse update(Long id, MovieRequest request) {
        Movie movie = findMovie(id);
        Set<Genre> genres = loadGenres(request.genreIds());
        movie.update(
                request.title().trim(),
                request.description(),
                request.durationMinutes(),
                request.ageRating(),
                request.status(),
                genres,
                clock.instant()
        );
        return MovieResponse.from(movie);
    }

    public void delete(Long id) {
        movieRepository.delete(findMovie(id));
    }

    private Movie findMovie(Long id) {
        return movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
    }

    private Set<Genre> loadGenres(Set<Long> genreIds) {
        List<Genre> genres = genreRepository.findAllById(genreIds);
        if (genres.size() < genreIds.size()) {
            throw new UnknownGenreException();
        }
        return new HashSet<>(genres);
    }
}
