package com.hnh6625.cinema.movie.service;

import com.hnh6625.cinema.movie.dto.GenreRequest;
import com.hnh6625.cinema.movie.dto.GenreResponse;
import com.hnh6625.cinema.movie.entity.Genre;
import com.hnh6625.cinema.movie.exception.GenreNameAlreadyExistsException;
import com.hnh6625.cinema.movie.exception.GenreNotFoundException;
import com.hnh6625.cinema.movie.repository.GenreRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class GenreService {
    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public GenreResponse create(GenreRequest request) {
        String name = request.name().trim();
        if (genreRepository.existsByNameIgnoreCase(name)) {
            throw new GenreNameAlreadyExistsException();
        }
        Genre saved = genreRepository.save(new Genre(name));
        return GenreResponse.from(saved);
    }

    public GenreResponse rename(Long id, GenreRequest request) {
        Genre genre = genreRepository.findById(id).orElseThrow(() -> new GenreNotFoundException(id));
        String name = request.name().trim();
        if (genreRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new GenreNameAlreadyExistsException();
        }
        genre.rename(name);
        return GenreResponse.from(genre);
    }

    public void delete(Long id) {
        Genre genre = genreRepository.findById(id).orElseThrow(() -> new GenreNotFoundException(id));
        genreRepository.delete(genre);
    }

    @Transactional(readOnly = true)
    public List<GenreResponse> findAll() {
        List<Genre> genres = genreRepository.findAll(Sort.by("name"));
        return genres.stream().map(GenreResponse::from).toList();
    }
}
