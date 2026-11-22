package com.hnh6625.cinema.movie.repository;

import com.hnh6625.cinema.AbstractIntegrationTest;
import com.hnh6625.cinema.movie.entity.AgeRating;
import com.hnh6625.cinema.movie.entity.Genre;
import com.hnh6625.cinema.movie.entity.Movie;
import com.hnh6625.cinema.movie.entity.MovieStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class MovieRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;

        private Movie newMovie(Set<Genre> genres, AgeRating ageRating) {
            return new Movie(
                    "Avengers",
                    "Hero movie",
                    120,
                    ageRating,
                    MovieStatus.NOW_SHOWING,
                    genres,
                    Instant.now()
                    );
        }

    @Test
    void save_movieWithGenres_persistsLinks() {
        Genre action = genreRepository.save(new Genre("Action"));
        Genre comedy = genreRepository.save(new Genre("Comedy"));

        Set<Genre> genres = Set.of(action,comedy);
        Movie savedMovie = newMovie(genres, AgeRating.T13);
        movieRepository.save(savedMovie);
        Long movieId = savedMovie.getId();

        entityManager.flush();
        entityManager.clear();

        Movie foundMovie = movieRepository.findById(movieId).orElseThrow();
        assertThat(foundMovie.getGenres())
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Action", "Comedy");
    }

    @Test
    void delete_movie_removesLinksButKeepsGenres() {
        Genre action = genreRepository.save(new Genre("Action"));
        Set<Genre> genres = Set.of(action);
        Movie savedMovie = newMovie(genres, AgeRating.T13);
        movieRepository.save(savedMovie);
        Long movieId = savedMovie.getId();
        Long genreId = action.getId();

        movieRepository.delete(savedMovie);

        entityManager.flush();
        entityManager.clear();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM movie_genres WHERE movie_id = ?",
                Integer.class,
                movieId
        );

        assertThat(count).isZero();
        assertThat(genreRepository.findById(genreId)).isPresent();
    }

    @Test
    void save_movie_storesEnumsAsText() {
        Genre action = genreRepository.save(new Genre("Action"));
        Set<Genre> genres = Set.of(action);
        Movie savedMovie = newMovie(genres, AgeRating.T13);
        movieRepository.save(savedMovie);
        Long movieId = savedMovie.getId();

        entityManager.flush();

        String sql = jdbcTemplate.queryForObject(
                "SELECT age_rating from movies where id = ?",
                String.class,
                movieId
                );

        assertThat(sql).isEqualTo("T13");
    }
}
