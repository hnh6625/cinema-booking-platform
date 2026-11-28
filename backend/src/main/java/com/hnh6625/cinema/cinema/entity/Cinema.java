package com.hnh6625.cinema.cinema.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cinemas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cinema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;

    @Version
    private Long version;

    public Cinema(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public void update(String name, String address) {
        this.name = name;
        this.address = address;
    }
}
