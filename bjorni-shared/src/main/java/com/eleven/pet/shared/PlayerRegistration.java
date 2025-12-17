package com.eleven.pet.shared;

import lombok.Data;

import java.io.Serializable;

@Data
public class PlayerRegistration implements Serializable {
    
    private String playerId;
    private String secretKey;
    
        public PlayerRegistration(String playerId, String secretKey) {
            this.playerId = playerId;
        this.secretKey = secretKey;
    }
    
}
