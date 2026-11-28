package com.hnh6625.cinema.cinema.service;

import com.hnh6625.cinema.cinema.dto.RoomRenameRequest;
import com.hnh6625.cinema.cinema.dto.RoomRequest;
import com.hnh6625.cinema.cinema.dto.RoomResponse;
import com.hnh6625.cinema.cinema.dto.SeatRowRequest;
import com.hnh6625.cinema.cinema.entity.Cinema;
import com.hnh6625.cinema.cinema.entity.Room;
import com.hnh6625.cinema.cinema.entity.Seat;
import com.hnh6625.cinema.cinema.exception.CinemaNotFoundException;
import com.hnh6625.cinema.cinema.exception.DuplicateSeatRowException;
import com.hnh6625.cinema.cinema.exception.RoomNameAlreadyExistsException;
import com.hnh6625.cinema.cinema.exception.RoomNotFoundException;
import com.hnh6625.cinema.cinema.repository.CinemaRepository;
import com.hnh6625.cinema.cinema.repository.RoomRepository;
import com.hnh6625.cinema.cinema.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class RoomService {
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final CinemaRepository cinemaRepository;

    public RoomService(RoomRepository roomRepository, SeatRepository seatRepository, CinemaRepository cinemaRepository) {
        this.roomRepository = roomRepository;
        this.seatRepository = seatRepository;
        this.cinemaRepository = cinemaRepository;
    }

    public RoomResponse create(Long cinemaId, RoomRequest request) {
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new CinemaNotFoundException(cinemaId));
        requireDistinctLabels(request.rows());
        String name = request.name().trim();
        if (roomRepository.existsByCinemaIdAndName(cinemaId, name)) {
            throw new RoomNameAlreadyExistsException(name);
        }
        Room room = roomRepository.save(new Room(cinema, name));
        List<Seat> seats = buildSeats(room, request.rows());
        seatRepository.saveAll(seats);
        return new RoomResponse(room.getId(), cinemaId, name, seats.size());
    }

    public RoomResponse rename(Long id, RoomRenameRequest request) {
        Room room = findRoom(id);
        String name = request.name().trim();
        Long cinemaId = room.getCinema().getId();
        if (roomRepository.existsByCinemaIdAndNameAndIdNot(cinemaId,name,id)) {
            throw new RoomNameAlreadyExistsException(name);
        }
        room.rename(name);
        int seatCount = Math.toIntExact(seatRepository.countByRoomId(id));
        return new RoomResponse(id, cinemaId, name,seatCount);
    }

    public void delete(Long id) {
        roomRepository.delete(findRoom(id));
    }

    private Room findRoom(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new RoomNotFoundException(id));
    }

    private String normalize(String label) {
        return label.trim().toUpperCase(Locale.ROOT);
    }

    private void requireDistinctLabels(List<SeatRowRequest> rows) {
        Set<String> seen = new HashSet<>();
        for (SeatRowRequest row : rows) {
            String label = normalize(row.label());
            if (!seen.add(label)) {
                throw new DuplicateSeatRowException(label);
            }
        }
    }

    private List<Seat> buildSeats(Room room, List<SeatRowRequest> rows) {
        List<Seat> seats = new ArrayList<>();

        for (SeatRowRequest row : rows) {
            String rowLabel = normalize(row.label());
            int seatCount = row.seatCount();

            for (int seatNumber = 1; seatNumber <= seatCount; seatNumber++) {
                Seat seat = new Seat(
                        room,
                        rowLabel,
                        seatNumber,
                        row.type()
                );
                seats.add(seat);
            }
        }
        return seats;

    }
}
