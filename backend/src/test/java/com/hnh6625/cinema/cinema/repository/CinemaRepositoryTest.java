package com.hnh6625.cinema.cinema.repository;

import com.hnh6625.cinema.AbstractIntegrationTest;
import com.hnh6625.cinema.cinema.entity.Cinema;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class CinemaRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void save_persistsFieldsAndStartsVersionAtZero() {
        Long id = cinemaRepository.save(new Cinema("CGV" , "Vo van van")).getId();
        entityManager.flush();
        entityManager.clear();

        Cinema found = cinemaRepository.findById(id).orElseThrow();

        assertThat(found.getName()).isEqualTo("CGV");
        assertThat(found.getAddress()).isEqualTo("Vo van van");
        assertThat(found.getVersion()).isEqualTo(0L);
    }

    @Test
    void update_incrementsVersion() {
        Cinema cinema = cinemaRepository.save(new Cinema("CGV", "Vo van van"));
        entityManager.flush();

        cinema.update("CGV Vo van van", "Vo van van");
        entityManager.flush();

        assertThat(cinema.getVersion()).isEqualTo(1L);
    }
}
