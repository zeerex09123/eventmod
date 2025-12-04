package ru.rebey.eventmod.data;

import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.team.PlayerTeam;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер для управления командами игроков.
 * Хранит информацию о команде каждого игрока.
 */
public class PlayerTeamManager {
    private static final String LOG_PREFIX = "[PlayerTeamManager] ";
    private static final Map<UUID, PlayerTeam> PLAYER_TEAMS = new ConcurrentHashMap<>();

    /**
     * Устанавливает команду игрока.
     * @param player игрок
     * @param team команда
     */
    public void setPlayerTeam(ServerPlayerEntity player, PlayerTeam team) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        if (team == null || team == PlayerTeam.NONE) {
            PlayerTeam removed = PLAYER_TEAMS.remove(uuid);
            EventMod.LOGGER.info("{}Удалена команда для игрока {} (была: {})",
                    LOG_PREFIX, playerName, removed);
        } else {
            PLAYER_TEAMS.put(uuid, team);
            EventMod.LOGGER.info("{}Установлена команда для игрока {}: {}",
                    LOG_PREFIX, playerName, team);
        }
    }

    /**
     * Устанавливает команду игрока по UUID.
     * @param uuid UUID игрока
     * @param team команда
     */
    public void setPlayerTeam(UUID uuid, PlayerTeam team) {
        if (team == null || team == PlayerTeam.NONE) {
            PlayerTeam removed = PLAYER_TEAMS.remove(uuid);
            EventMod.LOGGER.info("{}Удалена команда для UUID {} (была: {})",
                    LOG_PREFIX, uuid, removed);
        } else {
            PLAYER_TEAMS.put(uuid, team);
            EventMod.LOGGER.info("{}Установлена команда для UUID {}: {}",
                    LOG_PREFIX, uuid, team);
        }
    }

    /**
     * Получает команду игрока.
     * @param player игрок
     * @return команда игрока или NONE если не установлена
     */
    public PlayerTeam getPlayerTeam(ServerPlayerEntity player) {
        PlayerTeam team = PLAYER_TEAMS.getOrDefault(player.getUuid(), PlayerTeam.NONE);
        String playerName = player.getName().getString();

        EventMod.LOGGER.debug("{}Получена команда для игрока {}: {}",
                LOG_PREFIX, playerName, team);
        return team;
    }

    /**
     * Получает команду игрока по UUID.
     * @param uuid UUID игрока
     * @return команда игрока или NONE если не установлена
     */
    public PlayerTeam getPlayerTeam(UUID uuid) {
        PlayerTeam team = PLAYER_TEAMS.getOrDefault(uuid, PlayerTeam.NONE);
        EventMod.LOGGER.debug("{}Получена команда для UUID {}: {}", LOG_PREFIX, uuid, team);
        return team;
    }

    /**
     * Удаляет команду игрока.
     * @param uuid UUID игрока
     */
    public void removePlayerTeam(UUID uuid) {
        PlayerTeam removed = PLAYER_TEAMS.remove(uuid);
        if (removed != null) {
            EventMod.LOGGER.info("{}Удалена команда для UUID {}: {}", LOG_PREFIX, uuid, removed);
        }
    }

    /**
     * Получает количество игроков в команде.
     * @param team команда
     * @return количество игроков
     */
    public int getTeamSize(PlayerTeam team) {
        if (team == null || team == PlayerTeam.NONE) {
            return 0;
        }

        int count = (int) PLAYER_TEAMS.values().stream()
                .filter(t -> t == team)
                .count();

        EventMod.LOGGER.trace("{}Количество игроков в команде {}: {}", LOG_PREFIX, team, count);
        return count;
    }

    /**
     * Получает общее количество игроков с установленной командой.
     * @return количество игроков
     */
    public int getTotalPlayers() {
        int count = PLAYER_TEAMS.size();
        EventMod.LOGGER.trace("{}Всего игроков с командами: {}", LOG_PREFIX, count);
        return count;
    }

    /**
     * Проверяет, находится ли игрок в команде.
     * @param uuid UUID игрока
     * @param team команда для проверки
     * @return true если игрок в указанной команде
     */
    public boolean isPlayerInTeam(UUID uuid, PlayerTeam team) {
        PlayerTeam playerTeam = PLAYER_TEAMS.get(uuid);
        boolean inTeam = playerTeam == team;

        EventMod.LOGGER.trace("{}Проверка команды {} для UUID {}: {}",
                LOG_PREFIX, team, uuid, inTeam);
        return inTeam;
    }
}