package com.moviedates.backend.model;

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

    private LocalDateTime createdAt = LocalDateTime.now();

    // A session contains many Users (Polymorphism applied here)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private List<User> participants = new ArrayList<>();

    public boolean isActive(){
        if (createdAt.isBefore(LocalDateTime.now().minusHours(24))) {
            this.active = false;
        }
        return this.active;
    }
}