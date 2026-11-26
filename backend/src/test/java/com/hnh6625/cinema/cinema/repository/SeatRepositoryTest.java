package com.hnh6625.cinema.cinema.repository;

import com.hnh6625.cinema.AbstractIntegrationTest;
import com.hnh6625.cinema.cinema.entity.Cinema;
import com.hnh6625.cinema.cinema.entity.Room;
import com.hnh6625.cinema.cinema.entity.Seat;
import com.hnh6625.cinema.cinema.entity.SeatType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class SeatRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private EntityManager entityManager;

    private Room newRoom(String name) {
        Cinema cinema = cinemaRepository.save(new Cinema("CGV", "Vo Van Van"));
        return roomRepository.save(new Room(cinema, name));
    }

    @Test
    void countByRoomId_countsOnlySeatsOfThatRoom() {
        Room room1 = newRoom("Room 1");
        Room room2 = newRoom("Room 2");
        seatRepository.saveAll(List.of(
                new Seat(room1, "A", 1, SeatType.STANDARD),
                new Seat(room1, "A", 2, SeatType.STANDARD),
                new Seat(room1, "B", 1, SeatType.VIP)));
        seatRepository.save(new Seat(room2, "A", 1, SeatType.STANDARD));
        entityManager.flush();

        assertThat(seatRepository.countByRoomId(room1.getId())).isEqualTo(3);
        assertThat(seatRepository.countByRoomId(room2.getId())).isEqualTo(1);
    }

    @Test
    void countByRoomId_whenRoomHasNoSeats_returnsZero() {
        Room room = newRoom("Room 1");
        entityManager.flush();

        assertThat(seatRepository.countByRoomId(room.getId())).isEqualTo(0);
    }
}
