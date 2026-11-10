package com.hnh6625.cinema.movie.repository;

import com.hnh6625.cinema.AbstractIntegrationTest;
import com.hnh6625.cinema.movie.entity.Genre;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class GenreRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private GenreRepository repo;

    @Test
    void existsByNameIgnoreCase_whenStoredWithDifferentCase_returnsTrue() {
        repo.save(new Genre("Action"));

        boolean exists = repo.existsByNameIgnoreCase("ACTION");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByNameIgnoreCase_whenNotStored_returnsFalse() {
        repo.save(new Genre("Action"));

        boolean exists = repo.existsByNameIgnoreCase("COMEDY");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByNameIgnoreCaseAndIdNot_whenOnlyTheSameGenreHasName_returnsFalse() {
        Genre action = repo.save(new Genre("Action"));

        boolean exists = repo.existsByNameIgnoreCaseAndIdNot("action", action.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void existsByNameIgnoreCaseAndIdNot_whenAnotherGenreHasName_returnsTrue() {
        repo.save(new Genre("Action"));
        Genre comedy = repo.save(new Genre("Comedy"));

        boolean exists = repo.existsByNameIgnoreCaseAndIdNot("Action", comedy.getId());

        assertThat(exists).isTrue();
    }
}
