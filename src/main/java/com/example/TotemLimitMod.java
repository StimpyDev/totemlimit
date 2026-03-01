package com.example;

import com.example.mixin.TotemLimitMixin;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TotemLimitMod implements ModInitializer {
    public static final String MOD_ID = "totemlimit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // This cleans up the cooldown map when a player leaves to prevent memory leaks
    ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
        TotemManager.removePlayerData(handler.player.getUuid());
    });

        LOGGER.info("Totem Limit Mod initialized! Players can only carry 2 totems.");
    }
}
