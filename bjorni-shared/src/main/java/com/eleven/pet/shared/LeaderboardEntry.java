package com.eleven.pet.shared;

public record LeaderboardEntry(String playerName, boolean won, String gameName, long timeStamp) {
}
