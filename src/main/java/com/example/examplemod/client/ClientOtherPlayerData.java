package com.example.examplemod.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientOtherPlayerData {
    private static final Map<UUID, Integer> playerLevels = new HashMap<>();

    public static void setLevel(UUID uuid, int level) {
        playerLevels.put(uuid, level);
    }

    public static int getLevel(UUID uuid) {
        return playerLevels.getOrDefault(uuid, 1); // По умолчанию 1 уровень
    }

    public static void clear() {
        playerLevels.clear();
    }
}