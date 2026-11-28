package com.hnh6625.cinema.cinema.dto;

import com.hnh6625.cinema.cinema.entity.Cinema;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class CinemaResponseTest {
    @Test
    void from_mapsIdNameAndAddress() {
        Cinema cinema = new Cinema("CGV", "Vo Van Van");
        ReflectionTestUtils.setField(cinema, "id", 7L);

        CinemaResponse response = CinemaResponse.from(cinema);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.name()).isEqualTo("CGV");
        assertThat(response.address()).isEqualTo("Vo Van Van");
    }
}
