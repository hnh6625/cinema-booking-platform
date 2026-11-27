package com.hnh6625.cinema.cinema.service;

import com.hnh6625.cinema.cinema.dto.RoomRenameRequest;
import com.hnh6625.cinema.cinema.dto.RoomRequest;
import com.hnh6625.cinema.cinema.dto.RoomResponse;
import com.hnh6625.cinema.cinema.dto.SeatRowRequest;
import com.hnh6625.cinema.cinema.entity.Cinema;
import com.hnh6625.cinema.cinema.entity.Room;
import com.hnh6625.cinema.cinema.entity.Seat;
import com.hnh6625.cinema.cinema.entity.SeatType;
import com.hnh6625.cinema.cinema.exception.CinemaNotFoundException;
import com.hnh6625.cinema.cinema.exception.DuplicateSeatRowException;
import com.hnh6625.cinema.cinema.exception.RoomNameAlreadyExistsException;
import com.hnh6625.cinema.cinema.exception.RoomNotFoundException;
import com.hnh6625.cinema.cinema.repository.CinemaRepository;
import com.hnh6625.cinema.cinema.repository.RoomRepository;
import com.hnh6625.cinema.cinema.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private CinemaRepository cinemaRepository;

    private RoomService service;

    @BeforeEach
    void setUp() {
        service = new RoomService(roomRepository, seatRepository, cinemaRepository);
    }

    private Cinema cinemaWithId(Long id) {
        Cinema cinema = new Cinema("CGV", "Vo Van Van");
        ReflectionTestUtils.setField(cinema, "id", id);
        return cinema;
    }

    private Room roomWithId(Long id, Cinema cinema) {
        Room room = new Room(cinema, "Old name");
        ReflectionTestUtils.setField(room, "id", id);
        return room;
    }

    private SeatRowRequest row(String label, int seatCount, SeatType type) {
        return new SeatRowRequest(label, seatCount, type);
    }

    @Captor
    private ArgumentCaptor<List<Seat>> seatsCaptor;

    @Captor
    private ArgumentCaptor<Room> roomCaptor;

    @Test
    void create_whenCinemaMissing_throwsNotFoundAndTouchesNothing() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(1L,
                new RoomRequest("Room 1", List.of(row("A", 5, SeatType.STANDARD)))))
                .isInstanceOf(CinemaNotFoundException.class);

        verifyNoInteractions(roomRepository, seatRepository);
    }

    @Test
    void create_withDuplicateRowLabel_throwsBeforeCheckingName() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinemaWithId(1L)));

        assertThatThrownBy(() -> service.create(1L, new RoomRequest("Room 1",
                List.of(row("A", 5, SeatType.STANDARD), row("a", 5, SeatType.VIP)))))
                .isInstanceOf(DuplicateSeatRowException.class);

        verifyNoInteractions(roomRepository, seatRepository);
    }

    @Test
    void create_whenNameTaken_throwsConflictAndSavesNothing() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinemaWithId(1L)));
        when(roomRepository.existsByCinemaIdAndName(1L, "Room 1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(1L,
                new RoomRequest("Room 1", List.of(row("A", 5, SeatType.STANDARD)))))
                .isInstanceOf(RoomNameAlreadyExistsException.class);

        verify(roomRepository, never()).save(any());
        verifyNoInteractions(seatRepository);
    }

    @Test
    void create_withValidRequest_savesRoomAndGeneratedSeatsAndReturnsResponse() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinemaWithId(1L)));
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));
        when(roomRepository.existsByCinemaIdAndName(1L, "Room 1"))
                .thenReturn(false);

        RoomResponse response = service.create(1L, new RoomRequest("  Room 1  ",
                List.of(row("A", 3, SeatType.STANDARD),
                        row("b", 2, SeatType.VIP)
                )));

        verify(roomRepository).save(roomCaptor.capture());
        assertThat(roomCaptor.getValue().getName()).isEqualTo("Room 1");
        assertThat(roomCaptor.getValue().getCinema().getId()).isEqualTo(1L);

        verify(seatRepository).saveAll(seatsCaptor.capture());
        assertThat(seatsCaptor.getValue())
                .extracting(Seat::getRowLabel, Seat::getSeatNumber, Seat::getSeatType)
                .containsExactly(
                        tuple("A", 1, SeatType.STANDARD),
                        tuple("A", 2, SeatType.STANDARD),
                        tuple("A", 3, SeatType.STANDARD),
                        tuple("B", 1, SeatType.VIP),
                        tuple("B", 2, SeatType.VIP));

        assertThat(response.name()).isEqualTo("Room 1");
        assertThat(response.cinemaId()).isEqualTo(1L);
        assertThat(response.seatCount()).isEqualTo(5);


    }

    @Test
    void rename_whenMissing_throwsNotFound() {
        when(roomRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rename(5L, new RoomRenameRequest("Room 2")))
                .isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    void rename_whenNameTakenByAnotherRoom_throwsConflictAndKeepsName() {
        Room room = roomWithId(5L, cinemaWithId(1L));
        when(roomRepository.findById(5L)).thenReturn(Optional.of(room));
        when(roomRepository.existsByCinemaIdAndNameAndIdNot(1L, "Room 2", 5L)).thenReturn(true);

        assertThatThrownBy(() -> service.rename(5L, new RoomRenameRequest("Room 2")))
                .isInstanceOf(RoomNameAlreadyExistsException.class);

        assertThat(room.getName()).isEqualTo("Old name");
    }

    @Test
    void rename_withFreeName_changesNameWithoutSaveAndReturnsSeatCount() {
        Room room = roomWithId(5L, cinemaWithId(1L));
        when(roomRepository.findById(5L)).thenReturn(Optional.of(room));
        when(roomRepository.existsByCinemaIdAndNameAndIdNot(1L, "Room 2", 5L)).thenReturn(false);
        when(seatRepository.countByRoomId(5L)).thenReturn(12L);

        RoomResponse response = service.rename(5L, new RoomRenameRequest("  Room 2  "));

        assertThat(room.getName()).isEqualTo("Room 2");
        assertThat(response.name()).isEqualTo("Room 2");
        assertThat(response.seatCount()).isEqualTo(12);
        verify(roomRepository, never()).save(any());
    }

    @Test
    void delete_whenMissing_throwsNotFound() {
        when(roomRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(5L)).isInstanceOf(RoomNotFoundException.class);

        verify(roomRepository, never()).delete(any(Room.class));
    }

    @Test
    void delete_whenExists_deletesItWithoutLoadingSeats() {
        Room room = roomWithId(5L, cinemaWithId(1L));
        when(roomRepository.findById(5L)).thenReturn(Optional.of(room));

        service.delete(5L);

        verify(roomRepository).delete(room);
        verifyNoInteractions(seatRepository);
    }


}
