package com.eleven.pet.server.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Controller to handle "Live Audience" interactions during the presentation.
 * Allows the audience to interact via a web page and the Desktop client to poll for results.
 */
@RestController
@RequestMapping("/api/v1/live")
@CrossOrigin(origins = "*") // Allow access from mobile phones/external networks
public class LiveAudienceController {

    // Thread-safe counter for global pending treats (consumed by Desktop Client)
    private final AtomicInteger pendingTreats = new AtomicInteger(0);

    // In-memory storage for audience session data
    // Key: SessionToken (UUID), Value: AudienceMember
    private final Map<String, AudienceMember> audienceMembers = new ConcurrentHashMap<>();

    // --- DTOs ---
    public record LoginRequest(String name) {
    }

    public record LoginResponse(String token, String name) {
    }

    public record AudienceEntry(String name, int score) {
    }

    // --- Internal Model ---
    static class AudienceMember {
        final String name;
        final AtomicInteger score = new AtomicInteger(0);

        AudienceMember(String name) {
            this.name = name;
        }
    }

    /**
     * Audience Endpoint: Registers a new user session.
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody(required = false) LoginRequest request) {
        String name = (request != null && request.name() != null && !request.name().isBlank())
                ? request.name()
                : "Anonymous " + (audienceMembers.size() + 1);

        // Simple sanitization
        if (name.length() > 12) name = name.substring(0, 12);

        String token = UUID.randomUUID().toString();
        audienceMembers.put(token, new AudienceMember(name));

        return new LoginResponse(token, name);
    }

    /**
     * Audience Endpoint: Called when a user clicks "Give Treat".
     * Tracks individual score if a valid token is provided.
     */
    @PostMapping("/treat")
    public int giveTreat(@RequestHeader(value = "X-Session-Token", required = false) String token) {
        // 1. Increment individual score if user is known
        if (token != null && audienceMembers.containsKey(token)) {
            audienceMembers.get(token).score.incrementAndGet();
        }

        // 2. Always increment global pending treats for the visual effect on the projector
        return pendingTreats.incrementAndGet();
    }

    /**
     * Audience Endpoint: Returns the top contributors from the audience.
     */
    @GetMapping("/leaderboard")
    public List<AudienceEntry> getAudienceLeaderboard() {
        return audienceMembers.values().stream()
                .sorted((a, b) -> Integer.compare(b.score.get(), a.score.get())) // Descending order
                .limit(10) // Top 10
                .map(m -> new AudienceEntry(m.name, m.score.get()))
                .collect(Collectors.toList());
    }

    /**
     * Desktop Client Endpoint: The JavaFX app polls this to see how many treats were pending.
     * Resets the count to 0 after reading (consume behavior).
     */
    @GetMapping("/poll")
    public int getTreatsAndReset() {
        // Return current count and reset to 0 so we don't process them twice
        return pendingTreats.getAndSet(0);
    }
}