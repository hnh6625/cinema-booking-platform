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
public class RoomTableMigrationTest extends AbstractIntegrationTest {
    @Autowired
    private JdbcTemplate jdbc;

    private Long insertCinema(String name) {
        return jdbc.queryForObject("""
            insert into cinemas (name, address, version)
            values (?, 'Some address', 0)
            returning id
            """, Long.class, name);
    }

    private Long insertRoom(Long cinemaId, String name) {
        return jdbc.queryForObject("""
            insert into rooms (cinema_id, name, version)
            values (?, ?, 0)
            returning id
            """, Long.class, cinemaId, name);
    }

    private void insertSeat(Long roomId, String rowLabel, int number, String type) {
        jdbc.update("""
            insert into seats (room_id, row_label, seat_number, seat_type)
            values (?, ?, ?, ?)
            """, roomId, rowLabel, number, type);
    }

    private int count(String table) {
        return jdbc.queryForObject("select count(*) from " + table, Integer.class);
    }

    @Test
    void validRoomAndSeatCanBeInserted() {
        Long cinemaId = insertCinema("CGV");
        Long roomId = insertRoom(cinemaId, "Room 1");
        insertSeat(roomId, "A", 1, "STANDARD");

        assertThat(count("rooms")).isEqualTo(1);
        assertThat(count("seats")).isEqualTo(1);
    }

    @Test
    void roomNameMustBeUniqueWithinCinema() {
        Long cinemaId = insertCinema("CGV");
        insertRoom(cinemaId, "Room 1");

        assertThatThrownBy(() -> insertRoom(cinemaId, "Room 1"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void sameRoomNameIsAllowedInDifferentCinemas() {
        Long first = insertCinema("CGV");
        Long second = insertCinema("Lotte");
        insertRoom(first, "Room 1");
        insertRoom(second, "Room 1");

        assertThat(count("rooms")).isEqualTo(2);
    }

    @Test
    void roomNameMustNotBeBlank() {
        Long cinemaId = insertCinema("CGV");
        assertThatThrownBy(() -> insertRoom(cinemaId, "   "))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void cinemaWithRoomCannotBeDeleted() {
        Long cinemaId = insertCinema("CGV");
        insertRoom(cinemaId, "Room 1");
        assertThatThrownBy(() -> jdbc.update("delete from cinemas where id = ?", cinemaId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void seatMustBeUniqueByRowAndNumberWithInRoom() {
        Long cinemaId = insertCinema("CGV");
        Long roomId = insertRoom(cinemaId, "Room 1");
        insertSeat(roomId, "A", 1, "STANDARD");


        assertThatThrownBy(() -> insertSeat(roomId, "A", 1, "STANDARD"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
    @Test
    void sameSeatIsAllowedInDifferentRooms() {
        Long cinemaId = insertCinema("CGV");
        Long room1 = insertRoom(cinemaId,"Room 1");
        Long room2 = insertRoom(cinemaId, "Room 2");
        insertSeat(room1, "A", 1, "STANDARD");
        insertSeat(room2, "A", 1, "STANDARD");
        assertThat(count("seats")).isEqualTo(2);
    }

    @Test
    void seatTypeMustBeKnown() {
        Long cinemaId = insertCinema("CGV");
        Long roomId = insertRoom(cinemaId, "Room 1");

        assertThatThrownBy(() -> insertSeat(roomId, "A", 1, "UNKNOWN"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void seatNumberMustBePositive() {
        Long cinemaId = insertCinema("CGV");
        Long room1 = insertRoom(cinemaId,"Room 1");
        assertThatThrownBy(() -> insertSeat(room1, "A", 0, "STANDARD"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletingRoomRemovesItsSeats() {
        Long cinemaId = insertCinema("CGV");
        Long room1 = insertRoom(cinemaId,"Room 1");
        insertSeat(room1, "A", 1, "STANDARD");

        assertThat(count("seats")).isEqualTo(1);

        jdbc.update("delete from rooms where id = ?", room1);

        assertThat(count("seats")).isEqualTo(0);
    }
}
