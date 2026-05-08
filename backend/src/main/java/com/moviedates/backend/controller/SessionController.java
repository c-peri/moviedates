package com.moviedates.backend.controller;

import com.moviedates.backend.model.Session;
import com.moviedates.backend.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    // Triggered when the user clicks "Create Room"
    @PostMapping("/create")
    public ResponseEntity<Session> createRoom() {
        return ResponseEntity.ok(sessionService.createNewSession());
    }

    // Triggered when a user enters a code to join
    @GetMapping("/join/{code}")
    public ResponseEntity<Session> joinRoom(@PathVariable String code) {
        Session session = sessionService.getSessionByCode(code);
        if (session != null && session.isActive()) {
            return ResponseEntity.ok(session);
        }
        return ResponseEntity.status(404).body(null);
    }
}