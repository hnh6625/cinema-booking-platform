CREATE TABLE genres (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    name VARCHAR(50) NOT NULL ,
    CONSTRAINT ck_genres_name_not_blank CHECK ( length(trim(name)) > 0)
);

CREATE UNIQUE INDEX ux_genres_name_lower ON genres (lower(name));