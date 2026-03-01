package com.example;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TotemLimitMod implements ModInitializer {
	public static final String MOD_ID = "totemlimit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Totem Limit Mod initialized! Players can only carry 2 totems.");
	}
}
