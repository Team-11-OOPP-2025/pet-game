package com.eleven.pet.server.controller;

import com.eleven.pet.server.model.PlayerStats;
import com.eleven.pet.shared.model.LeaderboardEntry;
import com.eleven.pet.shared.util.Signature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/leaderboard")
@Tag(name = "Leaderboard", description = "Endpoints for submitting and retrieving leaderboard scores")
public class ScoreController {
    // Map<PlayerID, Stats> - Aggregates scores by the unique ID from the header
    // in a DB approach this would be a table with PlayerID as FK
    // Future improvement could be to use Redis or another in-memory DB for persistence across restarts
    private final ConcurrentHashMap<String, PlayerStats> playerStats = new ConcurrentHashMap<>();

    private final Signature signatureUtil = new Signature();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    private final AuthController authController;

    public ScoreController(AuthController authController) {
        this.authController = authController;
    }

    /**
     * Submits a player's score to the leaderboard.
     *
     * <p>
     * Endpoint: {@code POST /api/v1/leaderboard}
     * </p>
     *
     * @param clientSignature the cryptographic signature provided in the {@code X-HMAC-Signature} header.
     * @param playerId        the ID of the player provided in the {@code X-Player-ID} header.
     * @param rawJsonBody     the raw JSON body of the request containing the score submission.
     * @throws SecurityException if the player ID is unknown or the signature does not match.
     */
    @PostMapping
    @Operation(summary = "Submit a player's score",
            description = "Submits a player's score to the leaderboard with HMAC authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Score submitted successfully."),
            @ApiResponse(responseCode = "400", description = "Bad Request: Missing required headers."),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid player ID or signature."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error: An error occurred while processing the request.")
    })
    public ResponseEntity<String> submitScore(
            @RequestHeader(value = "X-HMAC-Signature") String clientSignature,
            @RequestHeader(value = "X-Player-ID") String playerId,
            @RequestBody String rawJsonBody) throws SecurityException {
        try {
            if (clientSignature == null || playerId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("400 Bad Request: Missing required headers.");
            }

            String secretKey = authController.getSharedKey(playerId);
            if (secretKey == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("403 Forbidden: You are not authorized to perform this action.");
            }

            String calculatedSignature = signatureUtil.calculateHMAC(rawJsonBody, secretKey);
            if (!calculatedSignature.equals(clientSignature)) {
                // Principle of the least knowledge: Do not reveal which part of the authentication failed
                // basic security practice to avoid giving clues to potential attackers
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("403 Forbidden: You are not authorized to perform this action.");
            }
            LeaderboardEntry entry = jsonMapper.readValue(rawJsonBody, LeaderboardEntry.class);

            // Update Stats (Using ID from Header as key)
            playerStats.compute(playerId, (_, stats) -> {
                if (stats == null) {
                    stats = new PlayerStats(entry.getPlayerName());
                }
                stats.playerName = entry.getPlayerName(); // Update name if changed
                stats.recordWin(entry.getGameName());
                return stats;
            });

            return ResponseEntity.ok("200 OK: Score submitted successfully.");
        } catch (Exception e) {
            // Should log the exception in a logging framework (e.g., Log4j, SLF4J)
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("500 Internal Server Error: An error occurred while processing the request.");
        }
    }

    /**
     * Retrieves the latest scores, sorted by timestamp.
     * <p>
     * Endpoint: {@code GET /api/v1/leaderboard}
     * </p>
     * The results are sorted in descending order (newest first).
     *
     * @param limit the maximum number of entries to return (default: 50).
     * @return a list of {@link LeaderboardEntry} objects.
     */
    @GetMapping
    @Operation(summary = "Retrieve top leaderboard scores",
            description = "Fetches the top scores from the leaderboard, sorted by score.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved leaderboard scores.")
    })
    public List<LeaderboardEntry> getScores(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        return playerStats.values().stream().map(stats -> {
                    // Reconstruction of LeaderboardEntry from PlayerStats
                    return new LeaderboardEntry(
                            stats.getPlayerName(),
                            stats.getTotalWins(),
                            stats.getTopPlayedGame(),
                            stats.getLastTimestamp()
                    );
                })
                // Sort by score descending (higher scores first)
                .sorted(Comparator.comparingLong(LeaderboardEntry::getScore).reversed())
                // Applt limit
                .limit(limit)
                // Collect to list
                .collect(Collectors.toList());
    }
}