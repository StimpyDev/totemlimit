package com.example;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TotemManager {
    private static final Map<UUID, Long> lastMessageTime = new HashMap<>();

    public static void removePlayerData(UUID uuid) {
        lastMessageTime.remove(uuid);
    }
    
    public static boolean isMessageOnCooldown(UUID uuid, long cooldownMs) {
        long now = System.currentTimeMillis();
        long lastTime = lastMessageTime.getOrDefault(uuid, 0L);
        
        if (now - lastTime >= cooldownMs) {
            lastMessageTime.put(uuid, now);
            return false;
        }
        return true;
    }
}
