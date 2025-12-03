// src/main/java/ru/rebey/eventmod/data/PlayerDataHandler.java
package ru.rebey.eventmod.data;

import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.team.PlayerTeam;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataHandler {
    private static final Map<UUID, PlayerClass> PLAYER_CLASS = new ConcurrentHashMap<>();
    private static final Map<UUID, PlayerTeam> PLAYER_TEAM = new ConcurrentHashMap<>();

    public static void setPlayerClass(ServerPlayerEntity player, PlayerClass playerClass) {
        if (playerClass == null) {
            PLAYER_CLASS.remove(player.getUuid());
        } else {
            PLAYER_CLASS.put(player.getUuid(), playerClass);
        }
    }

    public static PlayerClass getPlayerClass(ServerPlayerEntity player) {
        return PLAYER_CLASS.get(player.getUuid());
    }

    public static void setPlayerTeam(ServerPlayerEntity player, PlayerTeam team) {
        if (team == null || team == PlayerTeam.NONE) {
            PLAYER_TEAM.remove(player.getUuid());
        } else {
            PLAYER_TEAM.put(player.getUuid(), team);
        }
    }

    public static PlayerTeam getPlayerTeam(ServerPlayerEntity player) {
        return PLAYER_TEAM.getOrDefault(player.getUuid(), PlayerTeam.NONE);
    }

    public static void removePlayerData(UUID uuid) {
        PLAYER_CLASS.remove(uuid);
        PLAYER_TEAM.remove(uuid);
    }
}