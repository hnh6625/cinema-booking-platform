package com.hnh6625.cinema.common.time;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ClockConfigTest {
    @Test
    void clock_usesUtc() {
        Clock clock = new ClockConfig().clock();
        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    void clock_tracksSystemTime() {
        Clock clock = new ClockConfig().clock();

        Instant clockInstant = clock.instant();
        Instant now = Instant.now();
        Duration difference = Duration.between(clockInstant, now).abs();
        assertThat(difference).isLessThan(Duration.ofSeconds(1));
    }
}
