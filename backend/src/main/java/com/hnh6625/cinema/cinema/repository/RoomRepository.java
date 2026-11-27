package com.hnh6625.cinema.cinema.repository;

import com.hnh6625.cinema.cinema.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByCinemaIdAndName(Long cinemaId, String name);

    boolean existsByCinemaIdAndNameAndIdNot(Long cinemaId, String name, Long id);

    boolean existsByCinemaId(Long cinemaId);
}
