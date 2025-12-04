package ru.rebey.eventmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.data.PlayerDataHandler;

import java.util.Set;

/**
 * Команда для отображения активных эффектов игрока.
 * Не требует специальных разрешений.
 */
public class MyEffectsCommand {
    private static final String LOG_PREFIX = "[MyEffectsCommand] ";

    /**
     * Регистрирует команду /myeffects.
     */
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("myeffects")
                            .requires(source -> source.hasPermissionLevel(0))
                            .executes(MyEffectsCommand::execute)
            );
            EventMod.LOGGER.info("{}Команда /myeffects зарегистрирована", LOG_PREFIX);
        });
    }

    /**
     * Выполняет команду отображения эффектов.
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

        EventMod.LOGGER.debug("{}Игрок {} запросил список эффектов", LOG_PREFIX, player.getName().getString());

        Set<String> effects = PlayerDataHandler.getAllActiveEffects(player);
        String activeCard = PlayerDataHandler.getActiveCard(player);

        // Отправляем заголовок
        player.sendMessage(
                Text.literal("=== Ваши эффекты ===").formatted(Formatting.GOLD, Formatting.BOLD),
                false
        );

        // Активная карточка
        if (activeCard != null) {
            player.sendMessage(
                    Text.literal("Последняя карточка: ").formatted(Formatting.YELLOW)
                            .append(Text.literal(getCardDisplayName(activeCard)).formatted(Formatting.GREEN)),
                    false
            );
            EventMod.LOGGER.debug("{}Активная карточка игрока {}: {}",
                    LOG_PREFIX, player.getName().getString(), activeCard);
        }

        // Список эффектов
        if (effects.isEmpty()) {
            player.sendMessage(
                    Text.literal("Нет активных эффектов").formatted(Formatting.GRAY),
                    false
            );
            EventMod.LOGGER.debug("{}У игрока {} нет активных эффектов",
                    LOG_PREFIX, player.getName().getString());
        } else {
            player.sendMessage(
                    Text.literal("Активные эффекты (" + effects.size() + "):").formatted(Formatting.YELLOW),
                    false
            );

            for (String effectId : effects) {
                player.sendMessage(
                        Text.literal("  • ").formatted(Formatting.GRAY)
                                .append(Text.literal(getEffectDisplayName(effectId)).formatted(Formatting.WHITE)),
                        false
                );
            }

            EventMod.LOGGER.debug("{}Игрок {} имеет {} активных эффектов: {}",
                    LOG_PREFIX, player.getName().getString(), effects.size(), effects);
        }

        return 1;
    }

    /**
     * Получает отображаемое имя карточки по её ID.
     * @param cardId ID карточки
     * @return читаемое имя карточки
     */
    private static String getCardDisplayName(String cardId) {
        switch (cardId) {
            case "tank_card_1": return "🛡️ Щит души";
            case "tank_card_2": return "🦾 Железные суставы";
            case "tank_card_3": return "🛡️ Удар щитом";
            case "assassin_card_1": return "🎯 Охотник на целых";
            case "assassin_card_2": return "👻 Призрак ночи";
            case "assassin_card_3": return "⚡ Скорость разрушения";
            default:
                EventMod.LOGGER.warn("{}Неизвестный ID карточки: {}", LOG_PREFIX, cardId);
                return cardId;
        }
    }

    /**
     * Получает отображаемое имя эффекта по его ID.
     * @param effectId ID эффекта
     * @return читаемое имя эффекта
     */
    private static String getEffectDisplayName(String effectId) {
        switch (effectId) {
            case "extra_health_8": return "❤️ +4 сердца";
            case "slowness_1": return "🐌 Медлительность I";
            case "fall_damage_reduction": return "🦾 -50% урон от падения";
            case "shield_knockback": return "🛡️ Отталкивание щитом";
            case "hunger_drain": return "🍖 Быстрый голод";
            case "assassin_strength": return "🗡️ Сила убийцы";
            case "reduced_health_4": return "💔 -2 сердца";
            case "stealth_effect": return "👻 Стелс";
            case "leather_armor_only": return "🧥 Только слабая броня";
            case "increased_speed": return "⚡ Скорость";
            case "fire_inventory_destruction": return "🔥 Горение инвентаря";
            default:
                EventMod.LOGGER.warn("{}Неизвестный ID эффекта: {}", LOG_PREFIX, effectId);
                return effectId;
        }
    }
}