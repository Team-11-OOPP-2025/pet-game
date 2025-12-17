package com.eleven.pet.server.controller;

import com.eleven.pet.shared.PlayerRegistration;
import com.eleven.pet.shared.Signature;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    // Maps PlayerID -> SecretKey
    private final ConcurrentHashMap<String, String> validKeys = new ConcurrentHashMap<>();
    private final Signature signatureUtil = new Signature();

    @PostMapping("/register")
    public PlayerRegistration register() {
        String playerId = UUID.randomUUID().toString();
        String secretKey = UUID.randomUUID().toString();
        validKeys.put(playerId, secretKey);

        // return new PlayerRegistration(PlayerId, SecretKey);
        return new PlayerRegistration(playerId, secretKey);
    }

  

    public String getSharedKey(String playerId) {
        return validKeys.get(playerId);
    }
}
