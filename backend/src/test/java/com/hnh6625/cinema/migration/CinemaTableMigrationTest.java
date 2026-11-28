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
public class CinemaTableMigrationTest extends AbstractIntegrationTest {
    @Autowired
    private JdbcTemplate jdbc;

    private void insertCinema(String name, String address, Long version) {
        jdbc.update("""
            insert into cinemas (name, address, version)
            values (?, ?, ?)
            """, name, address, version);
    }

    @Test
    void validCinemaCanBeInserted() {
        insertCinema("CGV", "Vo Van Van", 0L);
        Integer count = jdbc.queryForObject("select count(*) from cinemas", Integer.class);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void nameMustNotBeBlank() {
        assertThatThrownBy(() -> insertCinema("  ", "Vo Van Van", 0L))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void addressMustNotBeBlank() {
        assertThatThrownBy(() -> insertCinema("CGV", "  ", 0L))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void versionIsRequired() {
        assertThatThrownBy(() -> insertCinema("CGV", "Vo Van Van", null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

