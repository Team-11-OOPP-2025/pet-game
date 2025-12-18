package com.eleven.pet.server;

import com.eleven.pet.server.controller.AuthController;
import com.eleven.pet.server.controller.ScoreController;
import com.eleven.pet.shared.model.LeaderboardEntry;
import com.eleven.pet.shared.util.Signature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScoreController.class)
@AutoConfigureJson
class ScoreControllerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Signature signatureUtil = new Signature();

    @Autowired
    private MockMvc mockMvc;

    // We MOCK the AuthController.
    // This means we define exactly what it returns without running its real logic.
    @MockitoBean
    private AuthController authController;


    @Test
    void submitScoreShouldAcceptValidSignature() throws Exception {
        // Mock player data
        String playerId = "test-player-id";
        String secretKey = "test-secret-key";

        // Tell the mock: "When asked for this ID's key, return 'test-secret-key'"
        when(authController.getSharedKey(playerId)).thenReturn(secretKey);

        // Mock entry data
        LeaderboardEntry entry = new LeaderboardEntry(
                "Bjorni", 100, "TimingGame", System.currentTimeMillis()
        );

        String jsonPayload = mapper.writeValueAsString(entry);
        String signature = signatureUtil.calculateHMAC(jsonPayload, secretKey);

        // WHEN: We perform the POST request
        mockMvc.perform(post("/api/v1/leaderboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Player-ID", playerId)
                        .header("X-HMAC-Signature", signature)
                        .content(jsonPayload))
                // Expect a 200 OK
                .andExpect(status().isOk());
    }

    @Test
    void submitScoreShouldRejectInvalidSignature() throws Exception {
        // Mock player data
        String playerId = "hacker-id";
        String secretKey = "hacker-key";

        when(authController.getSharedKey(playerId)).thenReturn(secretKey);

        // Prepare Payload
        LeaderboardEntry entry = new LeaderboardEntry(
                "Hacker", 9999, "TimingGame", System.currentTimeMillis()
        );
        String jsonPayload = mapper.writeValueAsString(entry);

        // Generate a WRONG signature (signed with different key)
        String wrongSignature = signatureUtil.calculateHMAC(jsonPayload, "wrong-key");

        // Expect Forbidden (403)
        mockMvc.perform(post("/api/v1/leaderboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Player-ID", playerId)
                        .header("X-HMAC-Signature", wrongSignature)
                        .content(jsonPayload))
                .andExpect(status().isForbidden());
    }
}