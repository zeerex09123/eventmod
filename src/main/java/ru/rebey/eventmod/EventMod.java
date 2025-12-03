// src/main/java/ru/rebey/eventmod/EventMod.java
package ru.rebey.eventmod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import ru.rebey.eventmod.card.CardTimer;
import ru.rebey.eventmod.command.CardsCommand;
import ru.rebey.eventmod.command.ModCommands;
import ru.rebey.eventmod.command.TeamCommand;
import ru.rebey.eventmod.data.PlayerEventHandler;
import ru.rebey.eventmod.network.*;
import ru.rebey.eventmod.playerlist.PlayerListSync;

public class EventMod implements ModInitializer {
    public static final String MOD_ID = "eventmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModCommands.register();
        TeamCommand.register();
        CardsCommand.register();

        // Пакеты
        OpenClassSelectionPayload.register();
        SelectClassPayload.register();
        OpenCardSelectionPayload.register();
        SelectCardPayload.register();
        SyncPlayerEventPlayerDataPayload.register();
        SyncAllPlayersPayload.register();
        TeamCommand.register();

        // Обработчики
        ClassSelectionHandler.register();
        CardSelectionHandler.register();
        PlayerEventHandler.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            PlayerListSync.register(server); // ← должно быть
            // CardTimer.register(server);
        });

        LOGGER.info("EventMod initialized successfully.");
    }
}