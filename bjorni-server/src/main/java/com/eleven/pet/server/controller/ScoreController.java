package com.eleven.pet.server.controller;

import com.eleven.pet.shared.LeaderboardEntry;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

// A better way to handle the api versioning would be through a middleware or filter
// but for simplicity, we are using the path versioning here.
// Time constraints also limit the implementation of advanced features like authentication,
@RestController
@RequestMapping("/api/v1/leaderboard")
public class ScoreController {
    // Replace this with a dependency-injected database or persistent storage
    private final List<LeaderboardEntry> scores = new ArrayList<>();

    /**
     * Endpoint to submit a new score.
     * POST http://localhost:8080/api/v{version}/leaderboard
     */
    @PostMapping
    public void submitScore(@RequestBody LeaderboardEntry entry) {
        System.out.println(" Received Score: " + entry);
        // TODO: Store based on a foreign key which is the game identifier
        scores.add(entry);
    }
}