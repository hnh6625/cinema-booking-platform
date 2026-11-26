package com.hnh6625.cinema.cinema.repository;

import com.hnh6625.cinema.cinema.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat,Long> {
    long countByRoomId(Long roomId);
}
