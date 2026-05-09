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
        // Guests don't have passwords, so we return null or empty
        return null;
    }

    @Override
    public String getUsername() {
        // For guests, we can use their unique ID or a "guest_" prefix
        return "guest_" + this.getId();
    }
}