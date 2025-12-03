// src/main/java/ru/rebey/eventmod/playerlist/PlayerListSync.java
package ru.rebey.eventmod.playerlist;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.data.PlayerClass;
import ru.rebey.eventmod.data.PlayerDataHandler;
import ru.rebey.eventmod.network.SyncAllPlayersPayload;

import java.util.ArrayList;
import java.util.List;

public class PlayerListSync {
    private static final int SYNC_INTERVAL = 20 * 2; // каждые 2 секунды

    public static void register(MinecraftServer server) {
        ServerTickEvents.END_SERVER_TICK.register(s -> {
            if (s.getTicks() % SYNC_INTERVAL == 0) {
                syncPlayerList(s);
            }
        });
    }

    public static void syncPlayerList(MinecraftServer server) {
        List<SyncAllPlayersPayload.PlayerInfo> players = new ArrayList<>();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerClass pc = PlayerDataHandler.getPlayerClass(player);
            String className = (pc != null) ? pc.getId() : "";
            String teamName = PlayerDataHandler.getPlayerTeam(player).name();
            players.add(new SyncAllPlayersPayload.PlayerInfo(player.getName().getString(), className, teamName));
        }
        SyncAllPlayersPayload payload = new SyncAllPlayersPayload(players);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}