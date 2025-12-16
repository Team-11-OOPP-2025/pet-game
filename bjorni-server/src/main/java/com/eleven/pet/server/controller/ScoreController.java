package com.eleven.pet.server.controller;

import com.eleven.pet.shared.LeaderboardEntry;
import com.eleven.pet.shared.Signature;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

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
    private final Signature signatureUtil = new Signature();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * Endpoint to submit a new score.
     * POST http://localhost:8080/api/v{version}/leaderboard
     */
    @PostMapping
    public void submitScore(@RequestHeader(value = "X-HMAC-Signature") String hmacSignature, @RequestBody LeaderboardEntry entry) {
        System.out.println(" Received Score: " + entry);
        // TODO: Replace "SHARED_KEY" with a secure key management solution
        String calculatedSignautre = signatureUtil.calculateHMAC(jsonMapper.writeValueAsString(entry), "SHARED_KEY");

        if (!calculatedSignautre.equals(hmacSignature)) {
            throw new SecurityException("Invalid HMAC signature");
        }

        // TODO: Store based on a foreign key which is the player identifier
        // In this simple example, we can use a dictionary or map to store total games won and (games won (name of game)) per player
        scores.add(entry);
    }

    // TODO: this is only for testing purposes, fix this to properly do as expected and to fetch data correctly
    @GetMapping
    public List<LeaderboardEntry> getScores() {
        return scores;
    }
}