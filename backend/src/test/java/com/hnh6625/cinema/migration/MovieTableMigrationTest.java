package com.hnh6625.cinema.migration;

import com.hnh6625.cinema.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
public class MovieTableMigrationTest extends AbstractIntegrationTest {
    @Autowired
    private JdbcTemplate jdbc;

    private Long insertMovie(int duration, String ageRating, String status) {
        return jdbc.queryForObject("""
            insert into movies (title, description, duration_minutes, age_rating, status,
                                version, created_at, updated_at)
            values ('Test movie', 'desc', ?, ?, ?, 0, now(), now())
            returning id
            """, Long.class, duration, ageRating, status);
    }

    private Long insertGenre(String name) {
        return jdbc.queryForObject(
                "insert into genres (name) values (?) returning id", Long.class, name
        );
    }

    private void link(Long movieId, Long genreId) {
        jdbc.update("insert into movie_genres (movie_id, genre_id) values (? , ?)", movieId, genreId);
    }

    @Test
    void validMovieCanBeInserted() {
        insertMovie(120, "T13", "NOW_SHOWING");

        Integer count = jdbc.queryForObject("select count(*) from movies", Integer.class);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void durationMustBePositive() {
        assertThatThrownBy(() -> insertMovie(0, "T13", "NOW_SHOWING"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void ageRatingMustBeKnown() {
        assertThatThrownBy(() -> insertMovie(120,"AAA", "NOW_SHOWING"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void statusMustBeKnown() {
        assertThatThrownBy(() -> insertMovie(120,"T13", "UNKNOWN"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletingMovieRemovesItsGenreLinks() {
        Long movieId = insertMovie(123,"T13","NOW_SHOWING");
        Long genreId = insertGenre("Action");
        link(movieId,genreId);

        jdbc.update("delete from movies where id = ?", movieId);

        Integer count = jdbc.queryForObject("select count(*) from movie_genres", Integer.class);
        assertThat(count).isEqualTo(0);
    }

    @Test
    void genreInUseCannotBeDeleted() {
        Long movieId = insertMovie(120, "T13","NOW_SHOWING");
        Long genreId = insertGenre("Action");
        link(movieId,genreId);

        assertThatThrownBy(() -> jdbc.update("delete from genres where id = ?", genreId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
