package com.moviedates.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("REGISTERED")
@Getter
@Setter
public class RegisteredUser extends User {

    private String password;

    @ElementCollection
    @CollectionTable(name = "user_preferred_genres", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "genre_id")
    private List<Integer> preferredGenres = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_favourite_movies", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "movie_id")
    private List<Integer> favouriteMovies = new ArrayList<>();

    @Override
    public boolean isGuest() {
        return false;
    }

    public enum AuthProvider {
        LOCAL, GOOGLE
    }

    @Enumerated(EnumType.STRING)
    private AuthProvider provider = AuthProvider.LOCAL;

    private String googleId;
}