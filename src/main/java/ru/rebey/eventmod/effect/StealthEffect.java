package ru.rebey.eventmod.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import ru.rebey.eventmod.EventMod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Эффект скрытности.
 * Делает игрока невидимым и замедленным после 3 секунд приседания.
 * После отпускания шифта дает ускорение на 5 секунд.
 */
public class StealthEffect {
    private static final String LOG_PREFIX = "[StealthEffect] ";

    // Время начала приседания для каждого игрока
    private static final Map<UUID, Long> SNEAK_START_TICK = new HashMap<>();

    // Флаг невидимости для каждого игрока
    private static final Map<UUID, Boolean> IS_STEALTHED = new HashMap<>();

    // Игроки с активным эффектом скрытности
    private static final Set<UUID> AFFECTED_PLAYERS = new HashSet<>();

    // Время приседания для активации скрытности (3 секунды = 60 тиков)
    private static final long STEALTH_DURATION_TICKS = 60;

    // Длительность эффектов при скрытности (10 секунд = 200 тиков)
    private static final int EFFECT_DURATION = 200;

    // Длительность ускорения после скрытности (5 секунд = 100 тиков)
    private static final int SPEED_BOOST_DURATION = 100;

    // Уровень ускорения после скрытности (1 = Speed II = +40%)
    private static final int SPEED_BOOST_AMPLIFIER = 1;

    static {
        EventMod.LOGGER.info("{}Инициализация эффекта скрытности", LOG_PREFIX);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            try {
                long currentTick = server.getTicks();
                int checkedPlayers = 0;
                int activatedStealth = 0;
                int deactivatedStealth = 0;

                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    if (isAffected(player)) {
                        checkedPlayers++;
                        updateStealth(player, currentTick, activatedStealth, deactivatedStealth);
                    }
                }

                // Периодическое логирование статистики
                if (currentTick % 6000 == 0) { // Каждые 5 минут
                    EventMod.LOGGER.debug("{}Статистика: игроков проверено: {}, всего с эффектом: {}, в скрытности: {}",
                            LOG_PREFIX, checkedPlayers, AFFECTED_PLAYERS.size(),
                            IS_STEALTHED.values().stream().filter(v -> v).count());
                }

            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при обработке эффекта скрытности: {}",
                        LOG_PREFIX, e.getMessage(), e);
            }
        });

        EventMod.LOGGER.info("{}Эффект скрытности инициализирован. Время до активации: {} тиков ({} секунд)",
                LOG_PREFIX, STEALTH_DURATION_TICKS, STEALTH_DURATION_TICKS / 20);
    }

    /**
     * Проверяет, имеет ли игрок эффект скрытности.
     * @param player игрок для проверки
     * @return true если эффект активен
     */
    private static boolean isAffected(ServerPlayerEntity player) {
        return AFFECTED_PLAYERS.contains(player.getUuid());
    }

    /**
     * Применяет эффект скрытности к игроку.
     * @param player игрок для применения эффекта
     */
    public static void apply(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        if (AFFECTED_PLAYERS.add(uuid)) {
            EventMod.LOGGER.info("{}Активация эффекта скрытности для игрока {}", LOG_PREFIX, playerName);

            // Отправляем информационное сообщение игроку
            player.sendMessage(
                    net.minecraft.text.Text.literal("👻 Эффект скрытности активирован").formatted(net.minecraft.util.Formatting.YELLOW),
                    true
            );
            player.sendMessage(
                    net.minecraft.text.Text.literal("  Приседайте 3 секунды для невидимости").formatted(net.minecraft.util.Formatting.GRAY),
                    false
            );

            EventMod.LOGGER.debug("{}Эффект применен к игроку {}. Всего игроков с эффектом: {}",
                    LOG_PREFIX, playerName, AFFECTED_PLAYERS.size());
        } else {
            EventMod.LOGGER.debug("{}Игрок {} уже имеет эффект скрытности", LOG_PREFIX, playerName);
        }
    }

    /**
     * Обновляет состояние скрытности игрока.
     * @param player игрок
     * @param currentTick текущий тик сервера
     * @param activatedStealth счетчик активаций скрытности
     * @param deactivatedStealth счетчик деактиваций скрытности
     */
    private static void updateStealth(ServerPlayerEntity player, long currentTick,
                                      int activatedStealth, int deactivatedStealth) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();
        boolean isSneaking = player.isSneaking();

        if (isSneaking) {
            // Игрок сидит на шифте
            Long startTick = SNEAK_START_TICK.get(uuid);
            if (startTick == null) {
                SNEAK_START_TICK.put(uuid, currentTick);
                startTick = currentTick;
                EventMod.LOGGER.trace("{}Игрок {} начал приседание", LOG_PREFIX, playerName);
            }

            // Через 3 секунды — активируем невидимость
            if (currentTick - startTick >= STEALTH_DURATION_TICKS && !IS_STEALTHED.getOrDefault(uuid, false)) {
                activateStealth(player);
                IS_STEALTHED.put(uuid, true);
                activatedStealth++;
            }

            // Убедимся, что эффекты активны
            if (IS_STEALTHED.getOrDefault(uuid, false)) {
                // Slowness I = -15% скорости
                if (!player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.SLOWNESS,
                            EFFECT_DURATION,
                            0, // Slowness I
                            false,
                            false
                    ));
                }
                // Invisibility = невидимость
                if (!player.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.INVISIBILITY,
                            EFFECT_DURATION,
                            0,
                            false,
                            false
                    ));
                }
            }
        } else {
            // Игрок отпустил шифт
            if (IS_STEALTHED.getOrDefault(uuid, false)) {
                deactivateStealth(player);
                IS_STEALTHED.put(uuid, false);
                deactivatedStealth++;
            }
            SNEAK_START_TICK.remove(uuid);
            EventMod.LOGGER.trace("{}Игрок {} прекратил приседание", LOG_PREFIX, playerName);
        }
    }

    /**
     * Активирует скрытность для игрока.
     * @param player игрок
     */
    private static void activateStealth(ServerPlayerEntity player) {
        String playerName = player.getName().getString();

        try {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.INVISIBILITY,
                    EFFECT_DURATION,
                    0,
                    false,
                    false
            ));
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS,
                    EFFECT_DURATION,
                    0, // Slowness I
                    false,
                    false
            ));

            EventMod.LOGGER.info("{}Скрытность активирована для игрока {} (длительность: {} секунд)",
                    LOG_PREFIX, playerName, EFFECT_DURATION / 20);

            // Отправляем уведомление игроку
            player.sendMessage(
                    net.minecraft.text.Text.literal("👻 Вы стали невидимы!").formatted(net.minecraft.util.Formatting.GREEN),
                    true
            );

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при активации скрытности для игрока {}: {}",
                    LOG_PREFIX, playerName, e.getMessage(), e);
        }
    }

    /**
     * Деактивирует скрытность для игрока.
     * @param player игрок
     */
    private static void deactivateStealth(ServerPlayerEntity player) {
        String playerName = player.getName().getString();

        try {
            player.removeStatusEffect(StatusEffects.INVISIBILITY);
            player.removeStatusEffect(StatusEffects.SLOWNESS);

            // Добавляем Speed II на 5 секунд
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED,
                    SPEED_BOOST_DURATION,
                    SPEED_BOOST_AMPLIFIER, // Speed II
                    false,
                    true
            ));

            EventMod.LOGGER.info("{}Скрытность деактивирована, ускорение применено к игроку {}",
                    LOG_PREFIX, playerName);

            // Отправляем уведомление игроку
            player.sendMessage(
                    net.minecraft.text.Text.literal("⚡ Получен импульс скорости!").formatted(net.minecraft.util.Formatting.AQUA),
                    true
            );

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при деактивации скрытности для игрока {}: {}",
                    LOG_PREFIX, playerName, e.getMessage(), e);
        }
    }

    /**
     * Удаляет эффект скрытности у игрока.
     * @param player игрок для удаления эффекта
     */
    public static void remove(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        if (AFFECTED_PLAYERS.remove(uuid)) {
            // Деактивируем скрытность если она активна
            if (IS_STEALTHED.getOrDefault(uuid, false)) {
                deactivateStealth(player);
                IS_STEALTHED.put(uuid, false);
            }

            SNEAK_START_TICK.remove(uuid);

            EventMod.LOGGER.info("{}Эффект скрытности снят с игрока {}. Осталось игроков: {}",
                    LOG_PREFIX, playerName, AFFECTED_PLAYERS.size());

            player.sendMessage(
                    net.minecraft.text.Text.literal("✅ Эффект скрытности снят").formatted(net.minecraft.util.Formatting.GREEN),
                    true
            );
        } else {
            EventMod.LOGGER.debug("{}Игрок {} не имел эффекта скрытности", LOG_PREFIX, playerName);
        }
    }

    /**
     * Проверяет, находится ли игрок в состоянии скрытности.
     * @param player игрок для проверки
     * @return true если игрок невидим
     */
    public static boolean isStealthed(ServerPlayerEntity player) {
        boolean isStealthed = IS_STEALTHED.getOrDefault(player.getUuid(), false);
        EventMod.LOGGER.trace("{}Проверка скрытности игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), isStealthed);
        return isStealthed;
    }

    /**
     * Получает время, которое игрок приседал.
     * @param player игрок
     * @return время приседания в тиках или 0 если не приседает
     */
    public static long getSneakTime(ServerPlayerEntity player) {
        Long startTick = SNEAK_START_TICK.get(player.getUuid());
        if (startTick == null) return 0;

        long currentTick = player.getServer().getTicks();
        long sneakTime = currentTick - startTick;

        EventMod.LOGGER.trace("{}Время приседания игрока {}: {} тиков",
                LOG_PREFIX, player.getName().getString(), sneakTime);

        return sneakTime;
    }

    /**
     * Получает оставшееся время до активации скрытности.
     * @param player игрок
     * @return оставшееся время в тиках или 0 если уже активировано
     */
    public static long getTimeUntilStealth(ServerPlayerEntity player) {
        if (isStealthed(player)) return 0;

        long sneakTime = getSneakTime(player);
        if (sneakTime == 0) return STEALTH_DURATION_TICKS;

        long timeRemaining = STEALTH_DURATION_TICKS - sneakTime;
        return Math.max(0, timeRemaining);
    }

    /**
     * Получает количество игроков с эффектом скрытности.
     * @return количество игроков
     */
    public static int getAffectedPlayerCount() {
        return AFFECTED_PLAYERS.size();
    }

    /**
     * Получает количество игроков в состоянии скрытности.
     * @return количество игроков
     */
    public static int getStealthedPlayerCount() {
        return (int) IS_STEALTHED.values().stream().filter(v -> v).count();
    }

    /**
     * Получает статистику эффекта.
     * @return строка со статистикой
     */
    public static String getStats() {
        return String.format("Stealth Stats: AffectedPlayers=%d, Stealthed=%d",
                AFFECTED_PLAYERS.size(), getStealthedPlayerCount());
    }
}