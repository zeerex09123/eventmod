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
 * Эффект невидимости при неподвижности.
 * Делает игрока невидимым после 10 секунд неподвижности.
 */
public class InvisibilityOnStillEffect {
    private static final String LOG_PREFIX = "[InvisibilityOnStillEffect] ";

    // Время последнего движения для каждого игрока
    private static final Map<UUID, Long> LAST_MOVE_TICK = new HashMap<>();

    // Флаг невидимости для каждого игрока
    private static final Map<UUID, Boolean> HAS_INVISIBILITY = new HashMap<>();

    // Игроки с активным эффектом
    private static final Set<UUID> AFFECTED_PLAYERS = new HashSet<>();

    // Время неподвижности для активации невидимости (10 секунд = 200 тиков)
    private static final long STILL_DURATION_TICKS = 200L;

    // Длительность эффекта невидимости при применении (10 секунд = 200 тиков)
    private static final int INVISIBILITY_DURATION = 200;

    static {
        EventMod.LOGGER.info("{}Инициализация эффекта невидимости при неподвижности", LOG_PREFIX);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            try {
                long currentTick = server.getTicks();
                int checkedPlayers = 0;
                int gainedInvisibility = 0;
                int lostInvisibility = 0;

                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    if (AFFECTED_PLAYERS.contains(player.getUuid())) {
                        checkedPlayers++;

                        Vec3d motion = player.getVelocity();
                        boolean isStill = motion.x == 0.0 && motion.z == 0.0 && !player.isSneaking();

                        if (isStill) {
                            Long lastMove = LAST_MOVE_TICK.getOrDefault(player.getUuid(), 0L);

                            // Проверяем, прошло ли достаточно времени для активации невидимости
                            if (currentTick - lastMove >= STILL_DURATION_TICKS) {
                                if (!HAS_INVISIBILITY.getOrDefault(player.getUuid(), false)) {
                                    applyInvisibility(player);
                                    HAS_INVISIBILITY.put(player.getUuid(), true);
                                    gainedInvisibility++;
                                }
                            }
                        } else {
                            // Игрок двигается — обновляем время последнего движения
                            LAST_MOVE_TICK.put(player.getUuid(), currentTick);

                            if (HAS_INVISIBILITY.getOrDefault(player.getUuid(), false)) {
                                removeInvisibility(player);
                                HAS_INVISIBILITY.put(player.getUuid(), false);
                                lostInvisibility++;
                            }
                        }
                    }
                }

                // Периодическое логирование статистики
                if (currentTick % 6000 == 0) { // Каждые 5 минут
                    EventMod.LOGGER.debug("{}Статистика: игроков проверено: {}, получили невидимость: {}, потеряли невидимость: {}, активных игроков: {}",
                            LOG_PREFIX, checkedPlayers, gainedInvisibility, lostInvisibility, AFFECTED_PLAYERS.size());
                }

            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при обработке эффекта невидимости при неподвижности: {}",
                        LOG_PREFIX, e.getMessage(), e);
            }
        });

        EventMod.LOGGER.info("{}Эффект невидимости при неподвижности инициализирован. Время до активации: {} тиков ({} секунд)",
                LOG_PREFIX, STILL_DURATION_TICKS, STILL_DURATION_TICKS / 20);
    }

    /**
     * Применяет эффект невидимости при неподвижности к игроку.
     * @param player игрок для применения эффекта
     */
    public static void apply(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        if (AFFECTED_PLAYERS.add(uuid)) {
            long currentTick = player.getServer().getTicks();
            LAST_MOVE_TICK.put(uuid, currentTick);
            HAS_INVISIBILITY.put(uuid, false);

            EventMod.LOGGER.info("{}Применение эффекта невидимости при неподвижности к игроку {}",
                    LOG_PREFIX, playerName);
            EventMod.LOGGER.debug("{}  Начальное время последнего движения: {}", LOG_PREFIX, currentTick);

            // Отправляем информационное сообщение игроку
            player.sendMessage(
                    net.minecraft.text.Text.literal("👻 Эффект невидимости активирован").formatted(net.minecraft.util.Formatting.YELLOW),
                    true
            );
            player.sendMessage(
                    net.minecraft.text.Text.literal("  Неподвижность 10 секунд → невидимость").formatted(net.minecraft.util.Formatting.GRAY),
                    false
            );

            EventMod.LOGGER.debug("{}Эффект применен к игроку {}. Всего игроков с эффектом: {}",
                    LOG_PREFIX, playerName, AFFECTED_PLAYERS.size());
        } else {
            EventMod.LOGGER.debug("{}Игрок {} уже имеет эффект невидимости при неподвижности",
                    LOG_PREFIX, playerName);
        }
    }

    /**
     * Применяет невидимость к игроку.
     * @param player игрок
     */
    private static void applyInvisibility(ServerPlayerEntity player) {
        String playerName = player.getName().getString();

        try {
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.INVISIBILITY,
                    INVISIBILITY_DURATION,
                    0,
                    false,
                    false
            ));

            EventMod.LOGGER.info("{}Применена невидимость к игроку {} (длительность: {} секунд)",
                    LOG_PREFIX, playerName, INVISIBILITY_DURATION / 20);

            // Отправляем уведомление игроку
            player.sendMessage(
                    net.minecraft.text.Text.literal("👻 Вы стали невидимы!").formatted(net.minecraft.util.Formatting.GREEN),
                    true
            );

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при применении невидимости к игроку {}: {}",
                    LOG_PREFIX, playerName, e.getMessage(), e);
        }
    }

    /**
     * Удаляет невидимость у игрока.
     * @param player игрок
     */
    private static void removeInvisibility(ServerPlayerEntity player) {
        String playerName = player.getName().getString();

        try {
            player.removeStatusEffect(StatusEffects.INVISIBILITY);
            EventMod.LOGGER.info("{}Удалена невидимость у игрока {}", LOG_PREFIX, playerName);

            // Отправляем уведомление игроку
            player.sendMessage(
                    net.minecraft.text.Text.literal("👻 Невидимость потеряна").formatted(net.minecraft.util.Formatting.RED),
                    true
            );

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при удалении невидимости у игрока {}: {}",
                    LOG_PREFIX, playerName, e.getMessage(), e);
        }
    }

    /**
     * Удаляет эффект невидимости при неподвижности у игрока.
     * @param player игрок для удаления эффекта
     */
    public static void remove(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        if (AFFECTED_PLAYERS.remove(uuid)) {
            // Удаляем невидимость если она активна
            if (HAS_INVISIBILITY.getOrDefault(uuid, false)) {
                removeInvisibility(player);
            }

            LAST_MOVE_TICK.remove(uuid);
            HAS_INVISIBILITY.remove(uuid);

            EventMod.LOGGER.info("{}Эффект невидимости при неподвижности снят с игрока {}. Осталось игроков: {}",
                    LOG_PREFIX, playerName, AFFECTED_PLAYERS.size());

            player.sendMessage(
                    net.minecraft.text.Text.literal("✅ Эффект невидимости снят").formatted(net.minecraft.util.Formatting.GREEN),
                    true
            );
        } else {
            EventMod.LOGGER.debug("{}Игрок {} не имел эффекта невидимости при неподвижности",
                    LOG_PREFIX, playerName);
        }
    }

    /**
     * Проверяет, имеет ли игрок эффект невидимости при неподвижности.
     * @param player игрок для проверки
     * @return true если эффект активен
     */
    public static boolean hasEffect(ServerPlayerEntity player) {
        boolean hasEffect = AFFECTED_PLAYERS.contains(player.getUuid());
        EventMod.LOGGER.trace("{}Проверка эффекта невидимости для игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), hasEffect);
        return hasEffect;
    }

    /**
     * Проверяет, невидим ли игрок в данный момент.
     * @param player игрок для проверки
     * @return true если игрок невидим
     */
    public static boolean isInvisible(ServerPlayerEntity player) {
        boolean isInvisible = HAS_INVISIBILITY.getOrDefault(player.getUuid(), false);
        EventMod.LOGGER.trace("{}Проверка невидимости игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), isInvisible);
        return isInvisible;
    }

    /**
     * Получает количество игроков с эффектом невидимости.
     * @return количество игроков
     */
    public static int getAffectedPlayerCount() {
        return AFFECTED_PLAYERS.size();
    }

    /**
     * Получает количество невидимых игроков.
     * @return количество игроков
     */
    public static int getInvisiblePlayerCount() {
        return (int) HAS_INVISIBILITY.values().stream().filter(v -> v).count();
    }

    /**
     * Получает статистику эффекта.
     * @return строка со статистикой
     */
    public static String getStats() {
        return String.format("InvisibilityOnStill Stats: AffectedPlayers=%d, Invisible=%d",
                AFFECTED_PLAYERS.size(), getInvisiblePlayerCount());
    }
}