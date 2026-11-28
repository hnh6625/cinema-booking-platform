package com.hnh6625.cinema.cinema.controller;

import com.hnh6625.cinema.cinema.dto.RoomRenameRequest;
import com.hnh6625.cinema.cinema.dto.RoomRequest;
import com.hnh6625.cinema.cinema.dto.RoomResponse;
import com.hnh6625.cinema.cinema.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/cinemas/{cinemaId}/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@PathVariable Long cinemaId,
                               @Valid @RequestBody RoomRequest request) {
        return roomService.create(cinemaId,request);
    }

    @PutMapping("/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RoomResponse rename(@PathVariable Long id,
                               @Valid @RequestBody RoomRenameRequest request) {
        return roomService.rename(id, request);
    }

    @DeleteMapping("/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        roomService.delete(id);
    }
}
