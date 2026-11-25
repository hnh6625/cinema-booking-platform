package com.hnh6625.cinema.movie.controller;

import com.hnh6625.cinema.AbstractIntegrationTest;
import com.hnh6625.cinema.auth.entity.Role;
import com.hnh6625.cinema.auth.entity.User;
import com.hnh6625.cinema.auth.repository.UserRepository;
import com.hnh6625.cinema.auth.service.TokenService;
import com.hnh6625.cinema.movie.entity.AgeRating;
import com.hnh6625.cinema.movie.entity.Genre;
import com.hnh6625.cinema.movie.entity.Movie;
import com.hnh6625.cinema.movie.entity.MovieStatus;
import com.hnh6625.cinema.movie.repository.GenreRepository;
import com.hnh6625.cinema.movie.repository.MovieRepository;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class MovieControllerTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private EntityManager entityManager;

    private String tokenFor(Role role) {
        User user = userRepository.save(new User("u@x.com", "+84901111111",
                passwordEncoder.encode("password123"), role, Instant.now()));
        return tokenService.createAccessToken(user);
    }

    private String movieJson(String title, String status, String genreIds) {
        return """
                {"title":"%s","description":"Hero movie","durationMinutes":120,
                 "ageRating":"T13","status":"%s","genreIds":[%s]}
                """.formatted(title, status, genreIds);
    }

    private ResultActions createMovie(String token, String body) throws Exception {
        MockHttpServletRequestBuilder request = post("/api/v1/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(request);
    }

    private Movie savedMovie(Genre genre) {
        return movieRepository.save(new Movie("Avengers", "Hero movie", 120,
                AgeRating.T13, MovieStatus.NOW_SHOWING, Set.of(genre), Instant.now()));
    }

    @Test
    void createMovie_asAdmin_returns201WithBody() throws Exception {
        Genre action = genreRepository.save(new Genre("Action"));
        String token = tokenFor(Role.ADMIN);

        createMovie(token, movieJson("Avengers", "NOW_SHOWING", String.valueOf(action.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Avengers"))
                .andExpect(jsonPath("$.genres[0].name").value("Action"));
    }

    @Test
    void createMovie_withoutToken_returns401() throws Exception {
        createMovie(null, movieJson("Avengers", "NOW_SHOWING", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createMovie_asUser_returns403() throws Exception {
        String token = tokenFor(Role.USER);

        createMovie(token, movieJson("Avengers", "NOW_SHOWING", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createMovie_withBlankTitle_returns400() throws Exception {
        String token = tokenFor(Role.ADMIN);

        createMovie(token, movieJson("   ", "NOW_SHOWING", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMovie_withUnknownGenreId_returns400() throws Exception {
        String token = tokenFor(Role.ADMIN);

        createMovie(token, movieJson("Avengers", "NOW_SHOWING", "999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMovie_asAdmin_returns200AndPersistsChanges() throws Exception {
        Genre action = genreRepository.save(new Genre("Action"));
        Long id = savedMovie(action).getId();
        String token = tokenFor(Role.ADMIN);

        mockMvc.perform(put("/api/v1/movies/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movieJson("Avengers 2", "ENDED", String.valueOf(action.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Avengers 2"))
                .andExpect(jsonPath("$.status").value("ENDED"));

        entityManager.flush();
        entityManager.clear();
        Movie reloaded = movieRepository.findById(id).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo("Avengers 2");
        assertThat(reloaded.getStatus()).isEqualTo(MovieStatus.ENDED);
    }

    @Test
    void updateMovie_unknownId_returns404() throws Exception {
        String token = tokenFor(Role.ADMIN);

        mockMvc.perform(put("/api/v1/movies/999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movieJson("Avengers", "NOW_SHOWING", "1")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMovie_asAdmin_returns204AndMovieIsGoneButGenreRemains() throws Exception {
        Genre action = genreRepository.save(new Genre("Action"));
        Long id = savedMovie(action).getId();
        String token = tokenFor(Role.ADMIN);

        mockMvc.perform(delete("/api/v1/movies/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();
        assertThat(movieRepository.findById(id)).isEmpty();
        assertThat(genreRepository.findById(action.getId())).isPresent();
    }

    @Test
    void deleteMovie_unknownId_returns404() throws Exception {
        String token = tokenFor(Role.ADMIN);

        mockMvc.perform(delete("/api/v1/movies/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void createMovie_withInvalidAgeRating_returns400() throws Exception {
        String token = tokenFor(Role.ADMIN);
        String body = """
                {"title":"Test","description":"Hero movie","durationMinutes":120,
                 "ageRating":"X","status":"Now_showing","genreIds":"1"}
                """;

        createMovie(token, body)
                .andExpect(status().isBadRequest());

    }
}
