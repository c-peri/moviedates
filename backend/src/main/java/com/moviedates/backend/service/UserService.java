package com.moviedates.backend.service;

import com.moviedates.backend.model.GuestUser;
import com.moviedates.backend.model.RegisteredUser;
import com.moviedates.backend.model.User;
import com.moviedates.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createGuest(String name) {
        GuestUser guest = new GuestUser();
        guest.setDisplayName(name);
        return userRepository.save(guest);
    }

    public User registerUser(String name, String email, String password) {
        RegisteredUser user = new RegisteredUser();
        user.setDisplayName(name);
        user.setEmail(email);
        user.setPassword(password);
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}