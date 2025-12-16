package com.eleven.pet.server.controller;

import com.eleven.pet.shared.PlayerRegistration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    // Maps PlayerID -> SecretKey
    private final ConcurrentHashMap<String, String> validKeys = new ConcurrentHashMap<>();

    @PostMapping("/register")
    public PlayerRegistration register() {
        // TODO: Generate a random secret key and player id (simple UUID for now, could be crypto-secure random)

        // return new PlayerRegistration(PlayerId, SecretKey);
        return null;
    }
}
