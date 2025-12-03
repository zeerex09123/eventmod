// src/main/java/ru/rebey/eventmod/data/PlayerEventHandler.java
package ru.rebey.eventmod.data;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import ru.rebey.eventmod.EventMod;

public class PlayerEventHandler {
    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PlayerDataHandler.removePlayerData(handler.getPlayer().getUuid());
            EventMod.LOGGER.debug("Cleared data for {}", handler.getPlayer().getName().getString());
        });
    }
}