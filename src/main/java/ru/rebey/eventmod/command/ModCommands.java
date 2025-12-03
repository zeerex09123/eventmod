// src/main/java/ru/rebey/eventmod/command/ModCommands.java
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
import ru.rebey.eventmod.network.OpenClassSelectionPayload;

public class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("chooseclass")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(ModCommands::openClassSelectionForAll));
        });
    }

    private static int openClassSelectionForAll(CommandContext<ServerCommandSource> context) {
        var server = context.getSource().getServer();
        var players = server.getPlayerManager().getPlayerList();
        if (players.isEmpty()) {
            context.getSource().sendError(Text.literal("Нет игроков"));
            return 0;
        }
        for (ServerPlayerEntity player : players) {
            ServerPlayNetworking.send(player, new OpenClassSelectionPayload());
            EventMod.LOGGER.info("Sent GUI to {}", player.getName().getString());
        }
        context.getSource().sendFeedback(() -> Text.literal("GUI отправлен " + players.size() + " игрокам"), true);
        return 1;
    }
}