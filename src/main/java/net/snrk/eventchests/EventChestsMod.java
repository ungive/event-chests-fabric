package net.snrk.eventchests;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventChestsMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("event-chests");

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
	}
}
