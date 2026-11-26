package com.hnh6625.cinema.cinema.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cinema_id")
    private Cinema cinema;

    private String name;
    @Version
    private Long version;

    public Room( Cinema cinema,String name) {
        this.cinema = cinema;
        this.name = name;
    }

    public void rename(String name) {
        this.name = name;
    }
}
