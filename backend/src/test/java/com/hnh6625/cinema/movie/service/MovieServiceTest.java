package com.hnh6625.cinema.movie.service;

import com.hnh6625.cinema.movie.dto.GenreResponse;
import com.hnh6625.cinema.movie.dto.MovieRequest;
import com.hnh6625.cinema.movie.dto.MovieResponse;
import com.hnh6625.cinema.movie.entity.AgeRating;
import com.hnh6625.cinema.movie.entity.Genre;
import com.hnh6625.cinema.movie.entity.Movie;
import com.hnh6625.cinema.movie.entity.MovieStatus;
import com.hnh6625.cinema.movie.exception.MovieNotFoundException;
import com.hnh6625.cinema.movie.exception.UnknownGenreException;
import com.hnh6625.cinema.movie.repository.GenreRepository;
import com.hnh6625.cinema.movie.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private GenreRepository genreRepository;

    private MovieService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new MovieService(movieRepository, genreRepository, clock);
    }

    private MovieRequest request(String title, Set<Long> genreIds) {
        return new MovieRequest(title, "desc", 120, AgeRating.T13, MovieStatus.NOW_SHOWING, genreIds);
    }

    private Genre genreWithId(Long id, String name) {
        Genre genre = new Genre(name);
        ReflectionTestUtils.setField(genre, "id", id);
        return genre;
    }

    private Movie movieWithId(Long id) {
        Movie movie = new Movie("Old title", "old", 90, AgeRating.P, MovieStatus.COMING_SOON,
                Set.of(), Instant.parse("2025-01-01T00:00:00Z"));
        ReflectionTestUtils.setField(movie, "id", id);
        return movie;
    }

    @Test
    void create_withValidRequest_savesTrimmedMovieWithGenresAndReturnsResponse() {
        Genre action = genreWithId(1L, "Action");
        when(genreRepository.findAllById(Set.of(1L))).thenReturn(List.of(action));
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

        MovieResponse response = service.create(request("  Avengers  ", Set.of(1L)));

        ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(captor.capture());
        Movie saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Avengers");
        assertThat(saved.getCreatedAt()).isEqualTo(NOW);
        assertThat(saved.getGenres()).containsExactly(action);
        assertThat(response.title()).isEqualTo("Avengers");
        assertThat(response.genres()).extracting(GenreResponse::name).containsExactly("Action");
    }

    @Test
    void update_whenMovieMissing_throwsNotFoundAndSkipsGenreLookup() {
        when(movieRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(5L, request("Avengers", Set.of(1L))))
                .isInstanceOf(MovieNotFoundException.class);

        verifyNoInteractions(genreRepository);
    }

    @Test
    void update_withValidRequest_updatesFieldsAndGenresWithoutSave() {
        Movie movie = movieWithId(5L);
        Genre comedy = genreWithId(2L, "Comedy");
        when(movieRepository.findById(5L)).thenReturn(Optional.of(movie));
        when(genreRepository.findAllById(Set.of(2L))).thenReturn(List.of(comedy));

        MovieResponse response = service.update(5L, request("  New title  ", Set.of(2L)));

        assertThat(movie.getTitle()).isEqualTo("New title");
        assertThat(movie.getDurationMinutes()).isEqualTo(120);
        assertThat(movie.getAgeRating()).isEqualTo(AgeRating.T13);
        assertThat(movie.getStatus()).isEqualTo(MovieStatus.NOW_SHOWING);
        assertThat(movie.getGenres()).containsExactly(comedy);
        assertThat(movie.getUpdatedAt()).isEqualTo(NOW);
        assertThat(response.title()).isEqualTo("New title");
        verify(movieRepository, never()).save(any());
    }

    @Test
    void delete_whenMissing_throwsNotFound() {
        when(movieRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(5L)).isInstanceOf(MovieNotFoundException.class);

        verify(movieRepository, never()).delete(any(Movie.class));
    }

    @Test
    void delete_whenExists_deletesIt() {
        Movie movie = movieWithId(5L);
        when(movieRepository.findById(5L)).thenReturn(Optional.of(movie));

        service.delete(5L);

        verify(movieRepository).delete(movie);
    }

    @Test
    void create_whenGenreIdUnknown_throwsAndSavesNothing() {
        Genre action = genreWithId(1L,"Action");

        when(genreRepository.findAllById(Set.of(1L,999L))).thenReturn(List.of(action));

        assertThatThrownBy(() -> service.create(request("Avengers", Set.of(1L,999L))))
                .isInstanceOf(UnknownGenreException.class);

        verify(movieRepository,never()).save(any());
    }

    @Test
    void update_whenGenreIdUnknown_throwsAndLeavesMovieUnchanged() {
        Movie movie = movieWithId(5L);
        Genre action = genreWithId(1L,"Action");
        when(movieRepository.findById(5L)).thenReturn(Optional.of(movie));
        when(genreRepository.findAllById(Set.of(1L,999L))).thenReturn(List.of(action));

        assertThatThrownBy(() -> service.update(5L, request("New title", Set.of(1L,999L))))
                .isInstanceOf(UnknownGenreException.class);
        assertThat(movie.getTitle()).isEqualTo("Old title");
        assertThat(movie.getGenres()).isEmpty();
        assertThat(movie.getUpdatedAt()).isEqualTo(Instant.parse("2025-01-01T00:00:00Z"));
    }
}