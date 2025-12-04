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

/**
 * Основные команды мода.
 * Включает команду для открытия выбора класса всем игрокам.
 */
public class ModCommands {
    private static final String LOG_PREFIX = "[ModCommands] ";

    /**
     * Регистрирует команды мода.
     */
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("chooseclass")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(ModCommands::openClassSelectionForAll));

            EventMod.LOGGER.info("{}Команда /chooseclass зарегистрирована", LOG_PREFIX);
        });
    }

    /**
     * Открывает окно выбора класса всем онлайн игрокам.
     * @param context контекст выполнения команды
     * @return количество игроков, получивших GUI
     */
    private static int openClassSelectionForAll(CommandContext<ServerCommandSource> context) {
        EventMod.LOGGER.info("{}Открытие выбора класса для всех игроков", LOG_PREFIX);

        var server = context.getSource().getServer();
        var players = server.getPlayerManager().getPlayerList();

        if (players.isEmpty()) {
            EventMod.LOGGER.warn("{}Нет онлайн игроков", LOG_PREFIX);
            context.getSource().sendError(Text.literal("Нет игроков"));
            return 0;
        }

        int sentCount = 0;
        for (ServerPlayerEntity player : players) {
            try {
                ServerPlayNetworking.send(player, new OpenClassSelectionPayload());
                EventMod.LOGGER.debug("{}GUI отправлен игроку {}", LOG_PREFIX, player.getName().getString());
                sentCount++;
            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при отправке GUI игроку {}: {}",
                        LOG_PREFIX, player.getName().getString(), e.getMessage(), e);
            }
        }

        context.getSource().sendFeedback(() -> Text.literal("GUI отправлен " + players.size() + " игрокам"), true);
        EventMod.LOGGER.info("{}GUI отправлен {} игрокам из {} онлайн", LOG_PREFIX, sentCount, players.size());
        return sentCount;
    }
}