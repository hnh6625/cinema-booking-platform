CREATE TABLE movies (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    title VARCHAR(255) NOT NULL ,
    description TEXT,
    duration_minutes INT NOT NULL,
    age_rating VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_movies_duration_positive CHECK ( duration_minutes > 0 ),
    CONSTRAINT ck_movies_age_rating CHECK ( age_rating IN ('P', 'K', 'T13', 'T16', 'T18') ),
    CONSTRAINT ck_movies_status CHECK ( status IN ('COMING_SOON', 'NOW_SHOWING', 'ENDED'))
);

CREATE TABLE movie_genres (
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    genre_id BIGINT NOT NULL REFERENCES genres(id),
    PRIMARY KEY (movie_id, genre_id)
);