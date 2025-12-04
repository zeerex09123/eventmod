package ru.rebey.eventmod.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.data.PlayerClass;
import ru.rebey.eventmod.data.PlayerDataHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Обработчик выбора карточек на сервере.
 * Принимает выбор карточки от клиента и применяет её эффекты.
 */
public class CardSelectionHandler {
    private static final String LOG_PREFIX = "[CardSelectionHandler] ";

    /**
     * Регистрирует обработчик выбора карточек.
     */
    public static void register() {
        EventMod.LOGGER.info("{}Регистрация обработчика выбора карточек", LOG_PREFIX);

        ServerPlayNetworking.registerGlobalReceiver(SelectCardPayload.ID, (payload, context) -> {
            try {
                ServerPlayerEntity player = context.player();
                String cardId = payload.cardId();
                String playerName = player.getName().getString();

                EventMod.LOGGER.info("{}Игрок {} выбрал карточку: {}",
                        LOG_PREFIX, playerName, cardId);

                // Проверяем класс игрока
                PlayerClass pc = PlayerDataHandler.getPlayerClass(player);
                if (pc == null) {
                    EventMod.LOGGER.warn("{}Игрок {} не имеет класса, выбор карточки отклонен",
                            LOG_PREFIX, playerName);

                    player.sendMessage(
                            Text.literal("❌ Сначала выберите класс!").formatted(Formatting.RED),
                            true
                    );
                    return;
                }

                EventMod.LOGGER.debug("{}Класс игрока {}: {}", LOG_PREFIX, playerName, pc);

                // Устанавливаем карточку (эффекты будут добавляться, а не заменяться)
                PlayerDataHandler.setActiveCard(player, cardId);

                // Синхронизируем эффекты с клиентом
                syncAllEffectsToClient(player);

                EventMod.LOGGER.info("{}Карточка {} успешно применена игроку {}",
                        LOG_PREFIX, cardId, playerName);

                // Отправляем подтверждение игроку
                player.sendMessage(
                        Text.literal("✅ Карточка выбрана!").formatted(Formatting.GREEN),
                        true
                );

            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при обработке выбора карточки: {}",
                        LOG_PREFIX, e.getMessage(), e);
            }
        });

        EventMod.LOGGER.info("{}Обработчик выбора карточек зарегистрирован", LOG_PREFIX);
    }

    /**
     * Синхронизирует все активные эффекты с клиентом.
     * @param player игрок для синхронизации
     */
    private static void syncAllEffectsToClient(ServerPlayerEntity player) {
        try {
            EventMod.LOGGER.debug("{}Синхронизация эффектов с клиентом для игрока {}",
                    LOG_PREFIX, player.getName().getString());

            // Создаем строку со всеми активными эффектами
            Set<String> allEffects = PlayerDataHandler.getAllActiveEffects(player);
            StringBuilder effectsStr = new StringBuilder();

            for (String effect : allEffects) {
                if (!effectsStr.isEmpty()) {
                    effectsStr.append(", ");
                }
                effectsStr.append(getEffectDisplayName(effect));
            }

            String finalStr = effectsStr.toString();
            if (finalStr.isEmpty()) {
                finalStr = "Нет активных эффектов";
            }

            EventMod.LOGGER.debug("{}Активные эффекты игрока {}: {}",
                    LOG_PREFIX, player.getName().getString(), allEffects.size());

            // Отправляем игроку сообщение с эффектами (для теста)
            player.sendMessage(
                    Text.literal("📋 Активные эффекты: " + finalStr)
                            .formatted(Formatting.GREEN),
                    false
            );

            EventMod.LOGGER.trace("{}Сообщение об эффектах отправлено игроку {}",
                    LOG_PREFIX, player.getName().getString());

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при синхронизации эффектов с клиентом: {}",
                    LOG_PREFIX, e.getMessage(), e);
        }
    }

    /**
     * Получает отображаемое имя эффекта по его ID.
     * @param effectId ID эффекта
     * @return читаемое имя эффекта
     */
    private static String getEffectDisplayName(String effectId) {
        String displayName;

        switch (effectId) {
            case "extra_health_8":
                displayName = "❤️ +4 сердца";
                break;
            case "slowness_1":
                displayName = "🐌 Медлительность I";
                break;
            case "fall_damage_reduction":
                displayName = "🦾 -50% урон от падения";
                break;
            case "shield_knockback":
                displayName = "🛡️ Отталкивание щитом";
                break;
            case "hunger_drain":
                displayName = "🍖 Быстрый голод";
                break;
            case "assassin_strength":
                displayName = "🗡️ Сила убийцы";
                break;
            case "reduced_health_4":
                displayName = "💔 -2 сердца";
                break;
            case "stealth_effect":
                displayName = "👻 Стелс";
                break;
            case "leather_armor_only":
                displayName = "🧥 Только кожа";
                break;
            case "increased_speed":
                displayName = "⚡ Скорость";
                break;
            case "fire_inventory_destruction":
                displayName = "🔥 Горение инвентаря";
                break;
            default:
                displayName = effectId;
                EventMod.LOGGER.warn("{}Неизвестный ID эффекта: {}", LOG_PREFIX, effectId);
                break;
        }

        EventMod.LOGGER.trace("{}Преобразование ID эффекта {} -> {}",
                LOG_PREFIX, effectId, displayName);

        return displayName;
    }

    /**
     * Получает статистику обработчика.
     * @return строка со статистикой
     */
    public static String getStats() {
        return "CardSelectionHandler: обработчик выбора карточек активен";
    }
}