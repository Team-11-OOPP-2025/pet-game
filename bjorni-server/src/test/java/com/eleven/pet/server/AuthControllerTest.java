package com.eleven.pet.server;

import com.eleven.pet.server.controller.AuthController;
import com.eleven.pet.shared.model.PlayerRegistration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureJson
class AuthControllerTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerShouldReturnNewPlayerCredentials() throws Exception {
        // WHEN: We perform the POST request
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON))
                // Expect a 200 OK
                .andExpect(status().isOk())
                .andReturn();

        // We manually deserialize to ensure we actually got a valid object back
        String jsonResponse = result.getResponse().getContentAsString();
        PlayerRegistration response = mapper.readValue(jsonResponse, PlayerRegistration.class);

        assertNotNull(response.getPlayerId(), "Response should contain a Player ID");
        assertNotNull(response.getSecretKey(), "Response should contain a Secret Key");
    }
}