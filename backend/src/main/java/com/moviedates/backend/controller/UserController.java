package com.moviedates.backend.controller;

import com.moviedates.backend.model.User;
import com.moviedates.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/guest")
    public User createGuest(@RequestParam String name) {
        return userService.createGuest(name);
    }
}