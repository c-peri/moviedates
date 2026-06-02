package com.moviedates.backend.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("GUEST")
public class GuestUser extends User {

    
    @Override
    public boolean isGuest() {
        return true;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return "Guest_" + this.getId();
    }
}