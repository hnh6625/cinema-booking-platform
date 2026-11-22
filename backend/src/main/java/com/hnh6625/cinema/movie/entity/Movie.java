package com.hnh6625.cinema.movie.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Table(name = "movies")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    private AgeRating ageRating;
    @Enumerated(EnumType.STRING)
    private MovieStatus status;

    @Version
    private Long version;

    @ManyToMany
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new HashSet<>();

    private Instant createdAt;
    private Instant updatedAt;

    public Movie(String title, String description, int durationMinutes, AgeRating ageRating,
                 MovieStatus status, Set<Genre> genres, Instant now) {
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.ageRating = ageRating;
        this.status = status;
        this.genres = new HashSet<>(genres);
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(String title, String description, int durationMinutes, AgeRating ageRating,
                       MovieStatus status, Set<Genre> genres, Instant now) {
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.ageRating = ageRating;
        this.status = status;
        this.genres.clear();
        this.genres.addAll(genres);
        this.updatedAt = now;
    }

}
