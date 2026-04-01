package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TotemLimitMod implements ModInitializer {
    public static final String MOD_ID = "totemlimit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (handler.player != null) {
                TotemManager.removePlayerData(handler.player.getUuid());
            }
        });

        LOGGER.info("Totem Limit Mod loaded.");
    }
}
