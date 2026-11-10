    package com.hnh6625.cinema.movie.exception;

    import com.hnh6625.cinema.common.exception.ConflictException;

    public class GenreNameAlreadyExistsException extends ConflictException {
        public GenreNameAlreadyExistsException() {
            super("Genre name is already used.");
        }
    }
