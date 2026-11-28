package com.hnh6625.cinema.cinema.controller;

import com.hnh6625.cinema.AbstractIntegrationTest;
import com.hnh6625.cinema.auth.entity.Role;
import com.hnh6625.cinema.auth.entity.User;
import com.hnh6625.cinema.auth.repository.UserRepository;
import com.hnh6625.cinema.auth.service.TokenService;
import com.hnh6625.cinema.cinema.entity.Cinema;
import com.hnh6625.cinema.cinema.entity.Room;
import com.hnh6625.cinema.cinema.repository.CinemaRepository;
import com.hnh6625.cinema.cinema.repository.RoomRepository;
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
public class CinemaControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private RoomRepository roomRepository;

    private String tokenFor(Role role) {
        User user = userRepository.save(new User("u@x.com", "+84901111111",
                passwordEncoder.encode("password123"), role, Instant.now()));
        return tokenService.createAccessToken(user);
    }

    private String cinemaJson(String name, String address) {
        return """
                {"name":"%s","address":"%s"}
                """.formatted(name, address);
    }

    private ResultActions createCinema(String token, String body) throws Exception {
        MockHttpServletRequestBuilder request = post("/api/v1/cinemas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(request);
    }

    @Test
    void createCinema_asAdmin_returns201WithBody() throws Exception {
        String token = tokenFor(Role.ADMIN);

        createCinema(token, cinemaJson("CGV", "Vo Van Van"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("CGV"))
                .andExpect(jsonPath("$.address").value("Vo Van Van"));
    }

    @Test
    void createCinema_withoutToken_returns401() throws Exception {
        createCinema(null, cinemaJson("CGV", "Vo Van Van"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCinema_asUser_returns403() throws Exception {
        String token = tokenFor(Role.USER);

        createCinema(token, cinemaJson("CGV", "Vo Van Van"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCinema_withBlankName_returns400() throws Exception {
        String token = tokenFor(Role.ADMIN);

        createCinema(token, cinemaJson("   ", "Vo Van Van"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCinema_asAdmin_returns200AndPersistsChanges() throws Exception {
        Long id = cinemaRepository.save(new Cinema("CGV", "Vo Van Van")).getId();
        String token = tokenFor(Role.ADMIN);

        mockMvc.perform(put("/api/v1/cinemas/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cinemaJson("CGV Vincom", "Dong Khoi")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("CGV Vincom"))
                .andExpect(jsonPath("$.address").value("Dong Khoi"));

        entityManager.flush();
        entityManager.clear();
        Cinema reloaded = cinemaRepository.findById(id).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("CGV Vincom");
        assertThat(reloaded.getAddress()).isEqualTo("Dong Khoi");
    }

    @Test
    void updateCinema_unknownId_returns404() throws Exception {
        String token = tokenFor(Role.ADMIN);

        mockMvc.perform(put("/api/v1/cinemas/999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cinemaJson("CGV", "Vo Van Van")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCinema_asAdmin_returns204AndCinemaIsGone() throws Exception {
        Long id = cinemaRepository.save(new Cinema("CGV", "Vo Van Van")).getId();
        String token = tokenFor(Role.ADMIN);

        mockMvc.perform(delete("/api/v1/cinemas/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();
        assertThat(cinemaRepository.findById(id)).isEmpty();
    }

    @Test
    void deleteCinema_unknownId_returns404() throws Exception {
        String token = tokenFor(Role.ADMIN);

        mockMvc.perform(delete("/api/v1/cinemas/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCinema_whenItHasRooms_returns409AndCinemaRemains() throws Exception {
        Cinema cinema = cinemaRepository.save(new Cinema("CGV", "Vo Van Van"));
        roomRepository.save(new Room(cinema, "Room 1"));
        String token = tokenFor(Role.ADMIN);

        mockMvc.perform(delete("/api/v1/cinemas/" + cinema.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());

        assertThat(cinemaRepository.findById(cinema.getId())).isPresent();
    }
}