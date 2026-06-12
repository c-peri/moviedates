package com.moviedates.backend.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    private boolean active = true;

    private boolean finished = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime swipingStartedAt;

    private Integer totalSwipes = 0;

    private long matchedMovieId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "session_movie_deck", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "movie_id")
    private List<Integer> movieDeck = new ArrayList<>();


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "session_participnats",
            joinColumns = @JoinColumn(name = "session_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIgnoreProperties({"sessions", "password", "preferredGenres", "role", "authorities"})
    private List<User> participants = new ArrayList<>();

    public boolean isActive(){
        if (this.active && createdAt.isBefore(LocalDateTime.now().minusHours(24))) {
            this.active = false;
        }
        return this.active;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "session_seen_movies", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "movie_id")
    private List<Integer> seenMovies = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "session_next_movie_deck", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "movie_id")
    private List<Integer> nextMovieDeck = new ArrayList<>();

    @Column(name = "deck_fetch_count")
    private int deckFetchCount = 0;

    @Column(name = "next_deck_requested")
    private boolean nextDeckRequested = false;
}