package com.hnh6625.cinema.cinema.service;

import com.hnh6625.cinema.cinema.dto.CinemaRequest;
import com.hnh6625.cinema.cinema.dto.CinemaResponse;
import com.hnh6625.cinema.cinema.entity.Cinema;
import com.hnh6625.cinema.cinema.exception.CinemaHasRoomsException;
import com.hnh6625.cinema.cinema.exception.CinemaNotFoundException;
import com.hnh6625.cinema.cinema.repository.CinemaRepository;
import com.hnh6625.cinema.cinema.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CinemaServiceTest {
    @Mock
    private CinemaRepository cinemaRepository;
    @Mock
    private RoomRepository roomRepository;

    private CinemaService service;

    @BeforeEach
    void setUp() {
        service = new CinemaService(cinemaRepository, roomRepository);
    }

    private Cinema cinemaWithId(Long id) {
        Cinema cinema = new Cinema("Old name", "Old address");
        ReflectionTestUtils.setField(cinema, "id", id);
        return cinema;
    }

    @Test
    void create_savesTrimmedCinemaAndReturnsResponse() {
        when(cinemaRepository.save(any(Cinema.class))).thenAnswer(inv -> inv.getArgument(0));

        CinemaResponse response = service.create(new CinemaRequest("   CGV    ", "  Vo Van Van   "));

        ArgumentCaptor<Cinema> captor = ArgumentCaptor.forClass(Cinema.class);
        verify(cinemaRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("CGV");
        assertThat(captor.getValue().getAddress()).isEqualTo("Vo Van Van");
        assertThat(response.name()).isEqualTo("CGV");
        assertThat(response.address()).isEqualTo("Vo Van Van");
    }

    @Test
    void update_withValidRequest_changesFieldsWithoutSave() {
        Cinema cinema = cinemaWithId(5L);
        when(cinemaRepository.findById(5L)).thenReturn(Optional.of(cinema));

        CinemaResponse response = service.update(5L, new CinemaRequest("  New Name   ", "  New Address  "));

        assertThat(cinema.getName()).isEqualTo("New Name");
        assertThat(cinema.getAddress()).isEqualTo("New Address");
        assertThat(response.name()).isEqualTo("New Name");
        verify(cinemaRepository, never()).save(any());
    }

    @Test
    void update_whenMissing_throwsNotFound() {
        when(cinemaRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(5L, new CinemaRequest("CGV", "Vo Van Van")))
                .isInstanceOf(CinemaNotFoundException.class);
    }

    @Test
    void delete_whenMissing_throwsNotFound() {
        when(cinemaRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(5L)).isInstanceOf(CinemaNotFoundException.class);

        verifyNoInteractions(roomRepository);;
        verify(cinemaRepository, never()).delete(any(Cinema.class));
    }

    @Test
    void delete_whenExist_deletesIt() {
        Cinema cinema = cinemaWithId(5L);
        when(roomRepository.existsByCinemaId(5L)).thenReturn(false);
        when(cinemaRepository.findById(5L)).thenReturn(Optional.of(cinema));

        service.delete(5L);

        verify(cinemaRepository).delete(cinema);
    }

    @Test
    void delete_whenCinemaHasRooms_throwsConflictAndDoesNotDelete() {
        Cinema cinema = cinemaWithId(5L);
        when(cinemaRepository.findById(5L)).thenReturn(Optional.of(cinema));
        when(roomRepository.existsByCinemaId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(5L))
                .isInstanceOf(CinemaHasRoomsException.class);

        verify(cinemaRepository, never()).delete(any(Cinema.class));
    }
}
