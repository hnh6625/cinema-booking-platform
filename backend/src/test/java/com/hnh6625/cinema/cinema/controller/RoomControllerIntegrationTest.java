package com.hnh6625.cinema.cinema.controller;

import com.hnh6625.cinema.AbstractIntegrationTest;
import com.hnh6625.cinema.auth.entity.Role;
import com.hnh6625.cinema.auth.entity.User;
import com.hnh6625.cinema.auth.repository.UserRepository;
import com.hnh6625.cinema.auth.service.TokenService;
import com.hnh6625.cinema.cinema.entity.Cinema;
import com.hnh6625.cinema.cinema.entity.Room;
import com.hnh6625.cinema.cinema.entity.Seat;
import com.hnh6625.cinema.cinema.entity.SeatType;
import com.hnh6625.cinema.cinema.repository.CinemaRepository;
import com.hnh6625.cinema.cinema.repository.RoomRepository;
import com.hnh6625.cinema.cinema.repository.SeatRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class RoomControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private EntityManager entityManager;

    private static final String VALID_ROWS = """
            [{"label":"A","seatCount":3,"type":"STANDARD"},
             {"label":"B","seatCount":2,"type":"VIP"}]
            """;

    private String tokenFor(Role role) {
        User user = userRepository.save(new User("u@x.com", "+84901111111",
                passwordEncoder.encode("password123"), role, Instant.now()));
        return tokenService.createAccessToken(user);
    }

    private Cinema savedCinema() {
        return cinemaRepository.save(new Cinema("CGV", "Vo Van Van"));
    }

    private String roomJson(String name, String rows) {
        return """
                {"name":"%s","rows":%s}
                """.formatted(name, rows);
    }

    private ResultActions createRoom(String token, Long cinemaId, String body) throws Exception {
        MockHttpServletRequestBuilder request = post("/api/v1/cinemas/" + cinemaId + "/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(request);
    }

    @Test
    void createRoom_asAdmin_returns201AndCreatesSeats() throws Exception {
        Cinema cinema = savedCinema();
        String token = tokenFor(Role.ADMIN);

        createRoom(token, cinema.getId(), roomJson("Room 1", VALID_ROWS))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.cinemaId").value(cinema.getId()))
                .andExpect(jsonPath("$.name").value("Room 1"))
                .andExpect(jsonPath("$.seatCount").value(5));

        assertThat(seatRepository.count()).isEqualTo(5);
    }

    @Test
    void createRoom_withoutToken_returns401() throws Exception {
        createRoom(null, 1L, roomJson("Room 1", VALID_ROWS))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createRoom_asUser_returns403() throws Exception {
        String token = tokenFor(Role.USER);

        createRoom(token, 1L, roomJson("Room 1", VALID_ROWS))
                .andExpect(status().isForbidden());
    }

    @Test
    void createRoom_forUnknownCinema_returns404() throws Exception {
        String token = tokenFor(Role.ADMIN);

        createRoom(token, 999L, roomJson("Room 1", VALID_ROWS))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRoom_withBlankName_returns400() throws Exception {
        Cinema cinema = savedCinema();
        String token = tokenFor(Role.ADMIN);

        createRoom(token, cinema.getId(), roomJson("   ", VALID_ROWS))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRoom_withInvalidRow_returns400() throws Exception {
        Cinema cinema = savedCinema();
        String token = tokenFor(Role.ADMIN);

        createRoom(token, cinema.getId(), roomJson("Room 1",
                """
                [{"label":"A","seatCount":0,"type":"STANDARD"}]
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRoom_withDuplicateRowLabels_returns400() throws Exception {
        Cinema cinema = savedCinema();
        String token = tokenFor(Role.ADMIN);

        createRoom(token, cinema.getId(), roomJson("Room 1",
                """
                [{"label":"A","seatCount":3,"type":"STANDARD"},
                 {"label":"a","seatCount":3,"type":"VIP"}]
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRoom_withUnknownSeatType_returns400() throws Exception {
        Cinema cinema = savedCinema();
        String token = tokenFor(Role.ADMIN);

        createRoom(token, cinema.getId(), roomJson("Room 1",
                """
                [{"label":"A","seatCount":3,"type":"COUPLE"}]
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRoom_withNameAlreadyUsedInCinema_returns409() throws Exception {
        Cinema cinema = savedCinema();
        roomRepository.save(new Room(cinema, "Room 1"));
        String token = tokenFor(Role.ADMIN);

        createRoom(token, cinema.getId(), roomJson("Room 1", VALID_ROWS))
                .andExpect(status().isConflict());
    }

    @Test
    void renameRoom_asAdmin_returns200AndPersistsName() throws Exception {
        Cinema cinema = savedCinema();
        Long roomId = roomRepository.save(new Room(cinema, "Room 1")).getId();
        String token = tokenFor(Role.ADMIN);
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(put("/api/v1/rooms/" + roomId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"IMAX"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("IMAX"));

        entityManager.flush();
        entityManager.clear();
        assertThat(roomRepository.findById(roomId).orElseThrow().getName()).isEqualTo("IMAX");
    }

    @Test
    void renameRoom_unknownId_returns404() throws Exception {
        String token = tokenFor(Role.ADMIN);

        mockMvc.perform(put("/api/v1/rooms/999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"IMAX"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void renameRoom_toNameUsedByAnotherRoom_returns409() throws Exception {
        Cinema cinema = savedCinema();
        roomRepository.save(new Room(cinema, "Room 1"));
        Long roomId = roomRepository.save(new Room(cinema, "Room 2")).getId();
        String token = tokenFor(Role.ADMIN);

        mockMvc.perform(put("/api/v1/rooms/" + roomId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Room 1"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteRoom_asAdmin_returns204AndRemovesRoomAndSeatsButNotCinema() throws Exception {
        Cinema cinema = savedCinema();
        Room room = roomRepository.save(new Room(cinema, "Room 1"));
        seatRepository.save(new Seat(room, "A", 1, SeatType.STANDARD));
        Long roomId = room.getId();
        String token = tokenFor(Role.ADMIN);
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(delete("/api/v1/rooms/" + roomId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();
        assertThat(roomRepository.findById(roomId)).isEmpty();
        assertThat(seatRepository.countByRoomId(roomId)).isEqualTo(0);
        assertThat(cinemaRepository.findById(cinema.getId())).isPresent();
    }

    @Test
    void deleteRoom_unknownId_returns404() throws Exception {
        String token = tokenFor(Role.ADMIN);

        mockMvc.perform(delete("/api/v1/rooms/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}