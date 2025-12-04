package ru.rebey.eventmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.data.PlayerDataHandler;

/**
 * Команда для сброса собственных эффектов карточек.
 * Не требует специальных разрешений.
 */
public class ResetMyEffectsCommand {
    private static final String LOG_PREFIX = "[ResetMyEffectsCommand] ";

    /**
     * Регистрирует команду /reseteffects.
     */
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("reseteffects")
                            .requires(source -> source.hasPermissionLevel(0))
                            .executes(ResetMyEffectsCommand::execute)
            );
            EventMod.LOGGER.info("{}Команда /reseteffects зарегистрирована", LOG_PREFIX);
        });
    }

    /**
     * Выполняет команду сброса эффектов.
     * @param context контекст выполнения команды
     * @return 1 при успехе, 0 при ошибке
     */
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            EventMod.LOGGER.warn("{}Команда вызвана не игроком", LOG_PREFIX);
            context.getSource().sendError(Text.literal("Эта команда может использоваться только игроком"));
            return 0;
        }

        EventMod.LOGGER.info("{}Игрок {} сбрасывает свои эффекты", LOG_PREFIX, player.getName().getString());
        PlayerDataHandler.resetAllEffects(player);

        // Сообщение для исполнителя
        context.getSource().sendFeedback(
                () -> Text.literal("✅ Ваши эффекты карточек сброшены!")
                        .formatted(net.minecraft.util.Formatting.GREEN),
                false
        );

        // Сообщение для игрока
        player.sendMessage(
                Text.literal("✅ Ваши эффекты карточек сброшены!")
                        .formatted(net.minecraft.util.Formatting.GREEN),
                false
        );

        return 1;
    }
}