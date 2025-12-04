package ru.rebey.eventmod.data;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import ru.rebey.eventmod.EventMod;

import java.util.UUID;

/**
 * Обработчик событий игрока.
 * Регистрирует события подключения и отключения игроков.
 */
public class PlayerEventHandler {
    private static final String LOG_PREFIX = "[PlayerEventHandler] ";

    /**
     * Регистрирует все обработчики событий игрока.
     */
    public static void register() {
        EventMod.LOGGER.info("{}Регистрация обработчиков событий игрока", LOG_PREFIX);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            try {
                String playerName = handler.getPlayer().getName().getString();
                UUID playerUuid = handler.getPlayer().getUuid();

                EventMod.LOGGER.info("{}Игрок {} отключается. Очистка данных...",
                        LOG_PREFIX, playerName);

                PlayerDataHandler.removePlayerData(playerUuid);

                EventMod.LOGGER.debug("{}Данные игрока {} очищены", LOG_PREFIX, playerName);
            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при обработке отключения игрока: {}",
                        LOG_PREFIX, e.getMessage(), e);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            try {
                String playerName = handler.getPlayer().getName().getString();
                EventMod.LOGGER.info("{}Игрок {} присоединился к серверу", LOG_PREFIX, playerName);
            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при обработке подключения игрока: {}",
                        LOG_PREFIX, e.getMessage(), e);
            }
        });

        EventMod.LOGGER.info("{}Обработчики событий игрока зарегистрированы", LOG_PREFIX);
    }
}