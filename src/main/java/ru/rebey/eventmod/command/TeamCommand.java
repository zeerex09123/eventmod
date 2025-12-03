// src/main/java/ru/rebey/eventmod/command/TeamCommand.java
package ru.rebey.eventmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.data.PlayerDataHandler;
import ru.rebey.eventmod.network.SyncAllPlayersPayload;
import ru.rebey.eventmod.team.PlayerTeam;

import java.util.ArrayList;
import java.util.List;

public class TeamCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("eventmod")
                            .requires(src -> src.hasPermissionLevel(2))
                            .then(CommandManager.literal("teams")
                                    .executes(TeamCommand::execute)
                            )
            );
        });
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        var server = context.getSource().getServer();
        var allPlayers = server.getPlayerManager().getPlayerList();
        if (allPlayers.isEmpty()) {
            context.getSource().sendError(Text.literal("Нет игроков"));
            return 0;
        }

        // Делим ВСЕХ игроков, независимо от класса
        java.util.Collections.shuffle(allPlayers);
        int mid = allPlayers.size() / 2;

        for (int i = 0; i < allPlayers.size(); i++) {
            PlayerTeam team = (i < mid) ? PlayerTeam.RED : PlayerTeam.BLUE;
            PlayerDataHandler.setPlayerTeam(allPlayers.get(i), team);
            EventMod.LOGGER.info("Assigned {} to {}", allPlayers.get(i).getName().getString(), team);
        }

        // Синхронизируем всех
        syncAllPlayers(server);

        context.getSource().sendFeedback(() -> Text.literal("Игроки разделены на команды!"), true);
        return 1;
    }

    private static void syncAllPlayers(net.minecraft.server.MinecraftServer server) {
        List<SyncAllPlayersPayload.PlayerInfo> list = new ArrayList<>();
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            String cls = "";
            var pc = PlayerDataHandler.getPlayerClass(p);
            if (pc != null) cls = pc.getId();
            String teamName = PlayerDataHandler.getPlayerTeam(p).name();
            list.add(new SyncAllPlayersPayload.PlayerInfo(p.getName().getString(), cls, teamName));
        }
        var payload = new SyncAllPlayersPayload(list);
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(p, payload);
        }
    }
}