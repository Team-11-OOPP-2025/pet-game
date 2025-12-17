package com.eleven.pet.shared;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class PlayerRegistration implements Serializable {
    
    private String playerId;
    private String secretKey;
    
    public PlayerRegistration(String playerId, String secretKey) {
        this.playerId = playerId;
        this.secretKey = secretKey;
    }
    
}
