package ru.rebey.eventmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.data.PlayerDataHandler;

import java.util.Collection;

/**
 * Команда для сброса эффектов карточек.
 * Поддерживает сброс собственных эффектов и эффектов других игроков.
 * Требует уровень разрешения 2.
 */
public class ResetEffectsCommand {
    private static final String LOG_PREFIX = "[ResetEffectsCommand] ";

    /**
     * Регистрирует команду /eventmod reseteffects.
     */
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("eventmod")
                            .requires(src -> src.hasPermissionLevel(2))
                            .then(CommandManager.literal("reseteffects")
                                    .executes(context -> resetEffectsForSelf(context))
                                    .then(CommandManager.argument("targets", EntityArgumentType.players())
                                            .executes(context -> resetEffectsForPlayers(context,
                                                    EntityArgumentType.getPlayers(context, "targets")))
                                    )
                            )
            );
            EventMod.LOGGER.info("{}Команда /eventmod reseteffects зарегистрирована", LOG_PREFIX);
        });
    }

    /**
     * Сбрасывает эффекты исполнителю команды.
     * @param context контекст выполнения команды
     * @return 1 при успехе, 0 при ошибке
     */
    private static int resetEffectsForSelf(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            EventMod.LOGGER.warn("{}Команда вызвана не игроком", LOG_PREFIX);
            context.getSource().sendError(Text.literal("Эта команда может использоваться только игроком"));
            return 0;
        }

        EventMod.LOGGER.info("{}Сброс эффектов для игрока {}", LOG_PREFIX, player.getName().getString());
        PlayerDataHandler.resetAllEffects(player);

        context.getSource().sendFeedback(
                () -> Text.literal("Ваши эффекты карточек сброшены").formatted(net.minecraft.util.Formatting.GREEN),
                true
        );

        return 1;
    }

    /**
     * Сбрасывает эффекты указанным игрокам.
     * @param context контекст выполнения команды
     * @param targets коллекция целевых игроков
     * @return количество игроков, у которых сброшены эффекты
     */
    private static int resetEffectsForPlayers(CommandContext<ServerCommandSource> context,
                                              Collection<ServerPlayerEntity> targets) {
        EventMod.LOGGER.info("{}Сброс эффектов для {} игроков", LOG_PREFIX, targets.size());

        int count = 0;
        for (ServerPlayerEntity player : targets) {
            try {
                PlayerDataHandler.resetAllEffects(player);
                count++;

                // Сообщаем игроку
                player.sendMessage(
                        Text.literal("Ваши эффекты карточек были сброшены администратором")
                                .formatted(net.minecraft.util.Formatting.YELLOW),
                        false
                );

                EventMod.LOGGER.debug("{}Эффекты сброшены для игрока {}", LOG_PREFIX, player.getName().getString());

            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при сбросе эффектов игроку {}: {}",
                        LOG_PREFIX, player.getName().getString(), e.getMessage(), e);
            }
        }

        if (count == 0) {
            EventMod.LOGGER.warn("{}Нет подходящих игроков для сброса эффектов", LOG_PREFIX);
            context.getSource().sendError(Text.literal("Нет подходящих игроков"));
            return 0;
        }

        // Создаем final копию переменной для использования в лямбде
        final int finalCount = count;
        context.getSource().sendFeedback(
                () -> Text.literal("Эффекты сброшены для " + finalCount + " игроков")
                        .formatted(net.minecraft.util.Formatting.GREEN),
                true
        );

        EventMod.LOGGER.info("{}Эффекты сброшены для {} игроков", LOG_PREFIX, finalCount);
        return count;
    }
}