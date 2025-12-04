package ru.rebey.eventmod.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.EventMod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Эффект экстренной регенерации.
 * Автоматически применяет регенерацию при низком здоровье, а затем голод.
 */
public class EmergencyRegenEffect {
    private static final String LOG_PREFIX = "[EmergencyRegenEffect] ";

    // Следим, получал ли игрок реген уже (чтобы не спамить)
    private static final Map<UUID, Boolean> HAS_TRIGGERED = new HashMap<>();

    // После регена — Hunger (запланированный)
    private static final Map<UUID, Long> HUNGER_SCHEDULED = new HashMap<>();

    // Порог здоровья для активации регенерации (30%)
    private static final float HEALTH_THRESHOLD = 0.3f;

    // Длительность регенерации (2000 тиков = 100 секунд)
    private static final int REGEN_DURATION = 2000;

    // Уровень регенерации (1 = II уровень)
    private static final int REGEN_AMPLIFIER = 1;

    // Задержка перед применением голода (100 тиков = 5 секунд)
    private static final int HUNGER_DELAY = 100;

    // Длительность голода (12000 тиков = 10 минут)
    private static final int HUNGER_DURATION = 12000;

    static {
        EventMod.LOGGER.info("{}Инициализация эффекта экстренной регенерации", LOG_PREFIX);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            try {
                long currentTick = server.getTicks();
                int checkedPlayers = 0;
                int triggeredRegen = 0;
                int appliedHunger = 0;

                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    checkedPlayers++;
                    UUID uuid = player.getUuid();
                    String playerName = player.getName().getString();

                    float health = player.getHealth();
                    float maxHealth = player.getMaxHealth();
                    float percent = health / maxHealth;

                    // Если HP < 30% и ещё не сработало
                    if (percent < HEALTH_THRESHOLD && !HAS_TRIGGERED.getOrDefault(uuid, false)) {
                        // Применяем регенерацию
                        player.addStatusEffect(new StatusEffectInstance(
                                StatusEffects.REGENERATION,
                                REGEN_DURATION,
                                REGEN_AMPLIFIER,
                                false,
                                true
                        ));

                        HAS_TRIGGERED.put(uuid, true);
                        triggeredRegen++;

                        // Запланировать Hunger через 5 секунд (100 тиков) — когда реген закончится
                        HUNGER_SCHEDULED.put(uuid, currentTick + HUNGER_DELAY);

                        EventMod.LOGGER.info("{}Активирована экстренная регенерация для игрока {}: {}/{} HP ({}%)",
                                LOG_PREFIX, playerName, health, maxHealth, Math.round(percent * 100));
                    }

                    // Проверяем, не пора ли Hunger
                    Long hungerTick = HUNGER_SCHEDULED.get(uuid);
                    if (hungerTick != null && currentTick >= hungerTick) {
                        player.addStatusEffect(new StatusEffectInstance(
                                StatusEffects.HUNGER,
                                HUNGER_DURATION,
                                0,
                                false,
                                true
                        ));

                        HUNGER_SCHEDULED.remove(uuid);
                        appliedHunger++;

                        EventMod.LOGGER.info("{}Применен голод после регенерации игроку {} (длительность: {} минут)",
                                LOG_PREFIX, playerName, HUNGER_DURATION / 1200);
                    }
                }

                // Периодическое логирование статистики
                if (currentTick % 6000 == 0) { // Каждые 5 минут
                    EventMod.LOGGER.debug("{}Статистика: игроков проверено: {}, регенераций: {}, голода: {}, триггеров: {}, запланировано: {}",
                            LOG_PREFIX, checkedPlayers, triggeredRegen, appliedHunger,
                            HAS_TRIGGERED.size(), HUNGER_SCHEDULED.size());
                }

            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при обработке экстренной регенерации: {}",
                        LOG_PREFIX, e.getMessage(), e);
            }
        });

        EventMod.LOGGER.info("{}Эффект экстренной регенерации инициализирован. Порог: {}%, Задержка голода: {} тиков",
                LOG_PREFIX, HEALTH_THRESHOLD * 100, HUNGER_DELAY);
    }

    /**
     * Применяет эффект экстренной регенерации к игроку.
     * Сбрасывает триггер при повторном применении.
     * @param player игрок для применения эффекта
     */
    public static void apply(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        // Сброс триггера при повторном применении (если карта получена снова)
        boolean hadTrigger = HAS_TRIGGERED.remove(uuid) != null;
        Long hadHungerScheduled = HUNGER_SCHEDULED.remove(uuid);

        EventMod.LOGGER.info("{}Применение эффекта экстренной регенерации для игрока {}. Было: триггер={}, запланирован голод={}",
                LOG_PREFIX, playerName, hadTrigger, hadHungerScheduled != null);
    }

    /**
     * Сбрасывает эффект для игрока.
     * @param player игрок для сброса эффекта
     */
    public static void resetForPlayer(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        boolean hadTrigger = HAS_TRIGGERED.remove(uuid) != null;
        Long hadHungerScheduled = HUNGER_SCHEDULED.remove(uuid);

        EventMod.LOGGER.info("{}Сброс эффекта экстренной регенерации для игрока {}. Удалено: триггер={}, запланирован голод={}",
                LOG_PREFIX, playerName, hadTrigger, hadHungerScheduled != null);
    }

    /**
     * Проверяет, активен ли эффект для игрока.
     * @param player игрок для проверки
     * @return true если эффект активен (триггер сработал или голод запланирован)
     */
    public static boolean isActive(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        boolean hasTriggered = HAS_TRIGGERED.getOrDefault(uuid, false);
        boolean hasHungerScheduled = HUNGER_SCHEDULED.containsKey(uuid);

        EventMod.LOGGER.trace("{}Проверка активности эффекта для игрока {}: triggered={}, hungerScheduled={}",
                LOG_PREFIX, player.getName().getString(), hasTriggered, hasHungerScheduled);

        return hasTriggered || hasHungerScheduled;
    }

    /**
     * Получает статистику эффекта.
     * @return строка со статистикой
     */
    public static String getStats() {
        return String.format("EmergencyRegen Stats: Triggered=%d, HungerScheduled=%d",
                HAS_TRIGGERED.size(), HUNGER_SCHEDULED.size());
    }
}