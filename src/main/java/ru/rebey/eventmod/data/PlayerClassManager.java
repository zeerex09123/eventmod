package ru.rebey.eventmod.data;

import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.EventMod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер для управления классами игроков.
 * Хранит информацию о классе каждого игрока.
 */
public class PlayerClassManager {
    private static final String LOG_PREFIX = "[PlayerClassManager] ";
    private static final Map<UUID, PlayerClass> PLAYER_CLASSES = new ConcurrentHashMap<>();

    /**
     * Устанавливает класс игрока.
     * @param player игрок
     * @param playerClass класс игрока
     */
    public void setPlayerClass(ServerPlayerEntity player, PlayerClass playerClass) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        if (playerClass == null) {
            PlayerClass removed = PLAYER_CLASSES.remove(uuid);
            EventMod.LOGGER.info("{}Удален класс для игрока {} (был: {})",
                    LOG_PREFIX, playerName, removed);
        } else {
            PLAYER_CLASSES.put(uuid, playerClass);
            EventMod.LOGGER.info("{}Установлен класс для игрока {}: {}",
                    LOG_PREFIX, playerName, playerClass);
        }
    }

    /**
     * Устанавливает класс игрока по UUID.
     * @param uuid UUID игрока
     * @param playerClass класс игрока
     */
    public void setPlayerClass(UUID uuid, PlayerClass playerClass) {
        if (playerClass == null) {
            PlayerClass removed = PLAYER_CLASSES.remove(uuid);
            EventMod.LOGGER.info("{}Удален класс для UUID {} (был: {})",
                    LOG_PREFIX, uuid, removed);
        } else {
            PLAYER_CLASSES.put(uuid, playerClass);
            EventMod.LOGGER.info("{}Установлен класс для UUID {}: {}",
                    LOG_PREFIX, uuid, playerClass);
        }
    }

    /**
     * Получает класс игрока.
     * @param player игрок
     * @return класс игрока или null если не установлен
     */
    public PlayerClass getPlayerClass(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();
        PlayerClass pc = PLAYER_CLASSES.get(uuid);

        EventMod.LOGGER.debug("{}Получен класс для игрока {}: {}",
                LOG_PREFIX, playerName, pc);
        return pc;
    }

    /**
     * Получает класс игрока по UUID.
     * @param uuid UUID игрока
     * @return класс игрока или null если не установлен
     */
    public PlayerClass getPlayerClass(UUID uuid) {
        PlayerClass pc = PLAYER_CLASSES.get(uuid);
        EventMod.LOGGER.debug("{}Получен класс для UUID {}: {}",
                LOG_PREFIX, uuid, pc);
        return pc;
    }

    /**
     * Удаляет класс игрока.
     * @param uuid UUID игрока
     */
    public void removePlayerClass(UUID uuid) {
        PlayerClass removed = PLAYER_CLASSES.remove(uuid);
        if (removed != null) {
            EventMod.LOGGER.info("{}Удален класс для UUID {}: {}",
                    LOG_PREFIX, uuid, removed);
        }
    }

    /**
     * Получает количество игроков с установленным классом.
     * @return количество игроков
     */
    public int getPlayerCount() {
        return PLAYER_CLASSES.size();
    }

    /**
     * Проверяет, установлен ли класс игроку.
     * @param uuid UUID игрока
     * @return true если класс установлен
     */
    public boolean hasPlayerClass(UUID uuid) {
        return PLAYER_CLASSES.containsKey(uuid);
    }
}