package com.hnh6625.cinema;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
public class GenreTableMigrationTest extends AbstractIntegrationTest {
    @Autowired
    private JdbcTemplate jdbc;

    private void insertGenre(String name) {
        jdbc.update("INSERT INTO genres (name) VALUES (?)", name);
    }

    @Test
    void validGenreCanBeInserted() {
        insertGenre("Action");
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM genres", Integer.class);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void nameMustBeUniqueIgnoringCase() {
        insertGenre("Action");
        assertThatThrownBy(() -> insertGenre("action")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void nameMustNotBeBlank() {
        assertThatThrownBy(() -> insertGenre("   ")).isInstanceOf(DataIntegrityViolationException.class);
    }
}
