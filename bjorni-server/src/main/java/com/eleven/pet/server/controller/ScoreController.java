package com.eleven.pet.server.controller;

import com.eleven.pet.shared.LeaderboardEntry;
import com.eleven.pet.shared.Signature;

import tools.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/leaderboard")
public class ScoreController {
    // Replace this with a dependency-injected database or persistent storage
    private final List<LeaderboardEntry> scores = new ArrayList<>();
    private final Signature signatureUtil = new Signature();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    private final AuthController authController;
    
    public ScoreController(AuthController authController) {
        this.authController = authController;
    }

    /**
     * Endpoint to submit a new score.
     * POST http://localhost:8080/api/v{version}/leaderboard
     */
    @PostMapping
    public void submitScore(
        @RequestHeader(value = "X-HMAC-Signature") String hmacSignature,
        @RequestHeader(value = "X-Player-ID") String playerId,
        @RequestBody LeaderboardEntry entry) throws Exception {

        // 1. Retrieve the specific key for this player
        String secretKey = authController.getSharedKey(playerId);

        // 2. Safety Check: If the key is null, this player isn't registered!
        if (secretKey == null) {
            throw new SecurityException("Unknown Player ID: " + playerId);
        }

        // 3. Re-calculate the HMAC using the retrieved key
        String jsonBody = jsonMapper.writeValueAsString(entry);
        String calculatedSignature = signatureUtil.calculateHMAC(jsonBody, secretKey);

        // 4. Verify match
        if (!calculatedSignature.equals(hmacSignature)) {
            throw new SecurityException("Invalid HMAC signature");
        }

        // If we pass all checks, accept the score
        synchronized (scores) {
            scores.add(entry);
        }
    }

    /**
     * Retrieves the latest scores.
     * GET http://localhost:8080/api/v{version}/leaderboard?limit=50
     */
    @GetMapping
    public List<LeaderboardEntry> getScores(@RequestParam(defaultValue = "50") int limit) {
        synchronized (scores) {
            return scores.stream()
                // FIX: Use 'timeStamp' with a capital S to match the Record definition
                .sorted(Comparator.comparingLong(LeaderboardEntry::timeStamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
        }
    }
}