package ru.rebey.eventmod.data;

import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.EventMod;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Менеджер для управления механикой ассасина.
 * Отслеживает атаки и определяет, когда применять бонусный урон.
 */
public class PlayerAssassinManager {
    private static final String LOG_PREFIX = "[PlayerAssassinManager] ";
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Кэш для предотвращения спама урона в один тик
    private static final ConcurrentHashMap<String, Long> RECENT_ATTACKS = new ConcurrentHashMap<>();
    private static final long ATTACK_COOLDOWN_MS = 100; // 0.1 секунды

    static {
        // Регистрация хука завершения работы для корректного завершения планировщика
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            EventMod.LOGGER.info("{}Завершение работы планировщика", LOG_PREFIX);
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));
    }

    /**
     * Проверяет, должен ли применяться бонусный урон ассасина.
     * @param assassin атакующий игрок (ассасин)
     * @param target целевой игрок
     * @return true если нужно применить бонусный урон
     */
    public boolean shouldApplyBonusDamage(ServerPlayerEntity assassin, ServerPlayerEntity target) {
        // Создаем уникальный ключ для этой пары атака-цель
        String attackKey = assassin.getUuid() + "-" + target.getUuid();
        Long lastAttackTime = RECENT_ATTACKS.get(attackKey);
        long currentTime = System.currentTimeMillis();

        // Проверяем кд (чтобы не спамить уроном в одном тике)
        if (lastAttackTime != null && currentTime - lastAttackTime < ATTACK_COOLDOWN_MS) {
            EventMod.LOGGER.debug("{}Атака от {} к {} находится на кд",
                    LOG_PREFIX, assassin.getName().getString(), target.getName().getString());
            return false;
        }

        // Обновляем время последней атаки
        RECENT_ATTACKS.put(attackKey, currentTime);
        EventMod.LOGGER.debug("{}Зарегистрирована атака от {} к {}",
                LOG_PREFIX, assassin.getName().getString(), target.getName().getString());

        // Через 1 секунду очищаем запись
        scheduler.schedule(() -> {
            RECENT_ATTACKS.remove(attackKey);
            EventMod.LOGGER.trace("{}Очищен кд атаки для ключа: {}", LOG_PREFIX, attackKey);
        }, 1, TimeUnit.SECONDS);

        return true;
    }

    /**
     * Отмечает цель как пораженную (для обратной совместимости).
     * @param assassinUuid UUID ассасина
     * @param targetUuid UUID цели
     */
    public void markTargetAsHit(UUID assassinUuid, UUID targetUuid) {
        EventMod.LOGGER.trace("{}Метод markTargetAsHit вызван (обратная совместимость): {} -> {}",
                LOG_PREFIX, assassinUuid, targetUuid);
    }

    /**
     * Отмечает цель как пораженную с таймаутом (для обратной совместимости).
     * @param assassinUuid UUID ассасина
     * @param targetUuid UUID цели
     * @param timeoutSeconds таймаут в секундах
     */
    public void markTargetAsHitWithTimeout(UUID assassinUuid, UUID targetUuid, long timeoutSeconds) {
        EventMod.LOGGER.trace("{}Метод markTargetAsHitWithTimeout вызван (обратная совместимость): {} -> {} ({} сек)",
                LOG_PREFIX, assassinUuid, targetUuid, timeoutSeconds);
    }

    /**
     * Проверяет, атаковал ли уже ассасин эту цель (для обратной совместимости).
     * @param assassinUuid UUID ассасина
     * @param targetUuid UUID цели
     * @return всегда false в новой реализации
     */
    public boolean hasAlreadyHitTarget(UUID assassinUuid, UUID targetUuid) {
        EventMod.LOGGER.trace("{}Метод hasAlreadyHitTarget вызван (обратная совместимость): {} -> {}",
                LOG_PREFIX, assassinUuid, targetUuid);
        return false;
    }

    /**
     * Сбрасывает цели ассасина (для обратной совместимости).
     * @param assassinUuid UUID ассасина
     */
    public void resetAssassinTargets(UUID assassinUuid) {
        EventMod.LOGGER.debug("{}Сброс целей ассасина: {}", LOG_PREFIX, assassinUuid);
    }

    /**
     * Удаляет данные ассасина (для обратной совместимости).
     * @param uuid UUID игрока
     */
    public void removeAssassinData(UUID uuid) {
        EventMod.LOGGER.debug("{}Удаление данных ассасина: {}", LOG_PREFIX, uuid);
    }
}