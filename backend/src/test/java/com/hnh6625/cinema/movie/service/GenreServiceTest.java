package com.hnh6625.cinema.movie.service;

import com.hnh6625.cinema.movie.dto.GenreRequest;
import com.hnh6625.cinema.movie.dto.GenreResponse;
import com.hnh6625.cinema.movie.entity.Genre;
import com.hnh6625.cinema.movie.exception.GenreNameAlreadyExistsException;
import com.hnh6625.cinema.movie.exception.GenreNotFoundException;
import com.hnh6625.cinema.movie.repository.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GenreServiceTest {
    @Mock
    private GenreRepository repository;

    private GenreService service;

    @BeforeEach
    void setUp() {
        service = new GenreService(repository);
    }

    @Test
    void create_withNewName_savesTrimmedNameAndReturnsResponse() {
        when(repository.save(any(Genre.class))).thenAnswer(inv -> inv.getArgument(0));

        GenreResponse response = service.create(new GenreRequest("  Action  "));

        ArgumentCaptor<Genre> captor = ArgumentCaptor.forClass(Genre.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Action");
        assertThat(response.name()).isEqualTo("Action");

    }

    @Test
    void create_whenNameExists_throwsConflictAndSavesNothing() {
        when(repository.existsByNameIgnoreCase("Action")).thenReturn(true);
        assertThatThrownBy(() -> service.create(new GenreRequest("Action")))
                .isInstanceOf(GenreNameAlreadyExistsException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void rename_whenGenreMissing_throwsNotFound() {
        when(repository.findById(1L))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.rename(1L, new GenreRequest("Comedy")))
                .isInstanceOf(GenreNotFoundException.class);
    }

    @Test
    void rename_toNameUsedByAnother_throwsConflict() {
        Genre genre = genreWithId(1L, "Action");
        when(repository.findById(1L)).thenReturn(Optional.of(genre));
        when(repository.existsByNameIgnoreCaseAndIdNot("Comedy", 1L)).thenReturn(true);
        assertThatThrownBy(() -> service.rename(1L, new GenreRequest("Comedy")))
                .isInstanceOf(GenreNameAlreadyExistsException.class);
        assertThat(genre.getName()).isEqualTo("Action");

    }

    @Test
    void rename_toFreeName_updatesName() {
        Genre genre = genreWithId(1L, "Action");
        when(repository.findById(1L)).thenReturn(Optional.of(genre));

        GenreResponse response = service.rename(1L, new GenreRequest("  Comedy  "));
        
        assertThat(genre.getName()).isEqualTo("Comedy");
        assertThat(response.name()).isEqualTo("Comedy");
    }

    @Test
    void delete_whenMissing_throwsNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(GenreNotFoundException.class);
        verify(repository, never()).delete(any(Genre.class));
    }

    @Test
    void delete_whenExist_deleteIt() {
        Genre genre = genreWithId(1L, "Action");
        when(repository.findById(1L)).thenReturn(Optional.of(genre));
        service.delete(1L);
        verify(repository).delete(genre);
    }

    @Test
    void findAll_returnsGenreSortedByName() {
        when(repository.findAll(Sort.by("name")))
                .thenReturn(List.of(genreWithId(1L, "Action"), genreWithId(2L, "Comedy")));
        List<GenreResponse> result = service.findAll();

        assertThat(result).extracting(GenreResponse::name).containsExactly("Action", "Comedy");
    }

    private Genre genreWithId(Long id, String name) {
        Genre genre = new Genre(name);
        ReflectionTestUtils.setField(genre, "id", id);
        return genre;
    }
}
