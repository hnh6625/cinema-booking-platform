package com.hnh6625.cinema.movie.dto;

import com.hnh6625.cinema.movie.entity.AgeRating;
import com.hnh6625.cinema.movie.entity.Genre;
import com.hnh6625.cinema.movie.entity.Movie;
import com.hnh6625.cinema.movie.entity.MovieStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class MovieResponseTest {
    @Test
    void from_copiesFieldsAndSortsGenresByName() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        Movie movie = new Movie("Avengers", "desc", 120, AgeRating.T13, MovieStatus.NOW_SHOWING,
                Set.of(new Genre("Comedy"), new Genre("Action")), now);
        ReflectionTestUtils.setField(movie, "id", 7L);

        MovieResponse response = MovieResponse.from(movie);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.title()).isEqualTo("Avengers");
        assertThat(response.durationMinutes()).isEqualTo(120);
        assertThat(response.ageRating()).isEqualTo(AgeRating.T13);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.genres()).extracting(GenreResponse::name)
                .containsExactly("Action", "Comedy");
    }

}
