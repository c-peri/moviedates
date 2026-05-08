package com.moviedates.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;

    private Integer movieId; // The ID from TMDB API

    private boolean accepted; // true = Right Swipe (Accept), false = Left Swipe (Reject)

    private LocalDateTime timestamp = LocalDateTime.now();
}