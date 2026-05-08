package com.moviedates.backend.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("REGISTERED")
@Getter
@Setter
public class RegisteredUser extends User {

    private String email;
    private String password;

    @Override
    public boolean isGuest() {
        return false;
    }
}