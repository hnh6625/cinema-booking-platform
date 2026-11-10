package com.hnh6625.cinema.movie.controller;

import com.hnh6625.cinema.AbstractIntegrationTest;
import com.hnh6625.cinema.auth.entity.Role;
import com.hnh6625.cinema.auth.entity.User;
import com.hnh6625.cinema.auth.repository.UserRepository;
import com.hnh6625.cinema.auth.service.TokenService;
import com.hnh6625.cinema.movie.entity.Genre;
import com.hnh6625.cinema.movie.repository.GenreRepository;
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
public class GenreControlllerTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenService tokenService;

    private String tokenFor(Role role) {
        User user = userRepository.save(new User("u@x.com", "+84901111111",
                passwordEncoder.encode("password123"), role, Instant.now()));
        return tokenService.createAccessToken(user);
    }

    private ResultActions createGenre(String token, String name) throws Exception {
        MockHttpServletRequestBuilder request = post("/api/v1/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"%s"}
                        """.formatted(name));
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(request);
    }

    @Test
    void createGenre_asAdmin_returns201WithBody() throws Exception {
        String token = tokenFor(Role.ADMIN);

        createGenre(token, "Action")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Action"));
    }

    @Test
    void createGenre_withoutToken_returns401() throws Exception {
        createGenre(null, "Action")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createGenre_asUser_returns403() throws Exception {
        String token = tokenFor(Role.USER);

        createGenre(token, "Action")
                .andExpect(status().isForbidden());
    }

    @Test
    void createGenre_withBlankName_returns400() throws Exception {
        String token = tokenFor(Role.ADMIN);

        createGenre(token, "   ")
                .andExpect(status().isBadRequest());
    }

    @Test
    void renameGenre_unknownId_returns404() throws Exception {
        String token = tokenFor(Role.ADMIN);

        mockMvc.perform(put("/api/v1/genres/999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Comedy"}
                            """))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGenres_withoutToken_returns200SortedByName() throws Exception {
        genreRepository.save(new Genre("Comedy"));
        genreRepository.save(new Genre("Action"));

        mockMvc.perform(get("/api/v1/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Action"))
                .andExpect(jsonPath("$[1].name").value("Comedy"));
    }

    @Test
    void createGenre_withDuplicateNameDifferentCase_returns409() throws Exception {
        genreRepository.save(new Genre("Action"));
        String token = tokenFor(Role.ADMIN);
        createGenre(token, "action")
                .andExpect(status().isConflict());
    }

    @Test
    void deleteGenre_asAdmin_returns204AndGenreIsGone() throws Exception {
        Long id = genreRepository.save(new Genre("Action")).getId();
        String token = tokenFor(Role.ADMIN);

        mockMvc.perform(delete("/api/v1/genres/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(genreRepository.findById(id)).isEmpty();
    }
}
