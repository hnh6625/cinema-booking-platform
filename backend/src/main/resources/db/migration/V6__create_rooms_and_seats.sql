CREATE TABLE rooms
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cinema_id BIGINT       NOT NULL REFERENCES cinemas (id),
    name      VARCHAR(100) NOT NULL,
    version   BIGINT       NOT NULL,
    CONSTRAINT uq_rooms_cinema_name UNIQUE (cinema_id, name),
    CONSTRAINT ck_rooms_name_not_blank CHECK (length(trim(name)) > 0)
);

CREATE TABLE seats
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    room_id     BIGINT      NOT NULL REFERENCES rooms (id) ON DELETE CASCADE,
    row_label   VARCHAR(3)  NOT NULL,
    seat_number INT         NOT NULL,
    seat_type   VARCHAR(20) NOT NULL,
    CONSTRAINT uq_seats_room_row_number UNIQUE (room_id, row_label, seat_number),
    CONSTRAINT ck_seats_number_positive CHECK (seat_number > 0),
    CONSTRAINT ck_seats_type CHECK (seat_type IN ('STANDARD', 'VIP'))
);