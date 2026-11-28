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
public class RoomRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void save_persistsNameAndCinemaAndStartsVersionAtZero() {
        Cinema cinema = cinemaRepository.save(new Cinema("CGV", "Vo Van Van"));
        Long roomId = roomRepository.save(new Room(cinema, "Room 1")).getId();
        entityManager.flush();
        entityManager.clear();

        Room found = roomRepository.findById(roomId).orElseThrow();

        assertThat(found.getName()).isEqualTo("Room 1");
        assertThat(found.getCinema().getId()).isEqualTo(cinema.getId());
        assertThat(found.getVersion()).isEqualTo(0L);
    }

    @Test
    void saveAll_seats_areCountedByRoom() {
        Cinema cinema = cinemaRepository.save(new Cinema("CGV", "Vo Van Van"));
        Room room = roomRepository.save(new Room(cinema, "Room 1"));
        seatRepository.saveAll(List.of(
                new Seat(room, "A", 1, SeatType.STANDARD),
                new Seat(room, "A", 2, SeatType.STANDARD),
                new Seat(room, "B", 1, SeatType.VIP)));
        entityManager.flush();

        assertThat(seatRepository.countByRoomId(room.getId())).isEqualTo(3);
    }

    @Test
    void deleteRoom_removesItsSeatsThroughDatabaseCascade() {
        Cinema cinema = cinemaRepository.save(new Cinema("CGV", "Vo Van Van"));
        Room room = roomRepository.save(new Room(cinema, "Room 1"));
        seatRepository.save(new Seat(room, "A", 1, SeatType.STANDARD));
        entityManager.flush();
        entityManager.clear();
        assertThat(seatRepository.countByRoomId(room.getId())).isEqualTo(1);

        roomRepository.delete(room);
        entityManager.flush();
        entityManager.clear();

        assertThat(seatRepository.countByRoomId(room.getId())).isEqualTo(0);
    }

}
