package ru.rebey.eventmod.data;

import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.team.PlayerTeam;

import java.util.Set;
import java.util.UUID;

/**
 * Главный координатор для управления всеми данными игрока.
 * Делегирует работу специализированным менеджерам.
 */
public class PlayerDataManager {
    private static final String LOG_PREFIX = "[PlayerDataManager] ";

    // Специализированные менеджеры
    private static final PlayerClassManager classManager = new PlayerClassManager();
    private static final PlayerTeamManager teamManager = new PlayerTeamManager();
    private static final PlayerCardManager cardManager = new PlayerCardManager();
    private static final PlayerEffectManager effectManager = new PlayerEffectManager();
    private static final PlayerDeathCacheManager deathCacheManager = new PlayerDeathCacheManager();
    private static final PlayerAssassinManager assassinManager = new PlayerAssassinManager();

    /**
     * Инициализация менеджера.
     */
    static {
        EventMod.LOGGER.info("{}Инициализация менеджера данных игрока", LOG_PREFIX);
        EventMod.LOGGER.info("{}Зарегистрировано менеджеров: 6", LOG_PREFIX);
    }

    // === Публичные методы для внешнего использования ===

    /**
     * Проверяет, должен ли применяться бонусный урон ассасина.
     * @param assassin атакующий игрок (ассасин)
     * @param target целевой игрок
     * @return true если нужно применить бонусный урон
     */
    public static boolean shouldApplyAssassinBonus(ServerPlayerEntity assassin, ServerPlayerEntity target) {
        boolean shouldApply = assassinManager.shouldApplyBonusDamage(assassin, target);
        EventMod.LOGGER.trace("{}Проверка бонусного урона ассасина {} -> {}: {}",
                LOG_PREFIX, assassin.getName().getString(), target.getName().getString(), shouldApply);
        return shouldApply;
    }

    /**
     * Получает все активные эффекты игрока по UUID.
     * @param uuid UUID игрока
     * @return множество ID эффектов
     */
    public static Set<String> getAllActiveEffects(UUID uuid) {
        Set<String> effects = effectManager.getAllActiveEffects(uuid);
        EventMod.LOGGER.trace("{}Получение эффектов для UUID {}: {}", LOG_PREFIX, uuid, effects.size());
        return effects;
    }

    /**
     * Устанавливает класс игрока.
     * @param player игрок
     * @param playerClass класс игрока
     */
    public static void setPlayerClass(ServerPlayerEntity player, PlayerClass playerClass) {
        EventMod.LOGGER.debug("{}Установка класса для игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), playerClass);
        classManager.setPlayerClass(player, playerClass);
    }

    /**
     * Получает класс игрока.
     * @param player игрок
     * @return класс игрока или null
     */
    public static PlayerClass getPlayerClass(ServerPlayerEntity player) {
        PlayerClass pc = classManager.getPlayerClass(player);
        EventMod.LOGGER.trace("{}Получение класса для игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), pc);
        return pc;
    }

    /**
     * Устанавливает команду игрока.
     * @param player игрок
     * @param team команда
     */
    public static void setPlayerTeam(ServerPlayerEntity player, PlayerTeam team) {
        EventMod.LOGGER.debug("{}Установка команды для игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), team);
        teamManager.setPlayerTeam(player, team);
    }

    /**
     * Получает команду игрока.
     * @param player игрок
     * @return команда игрока
     */
    public static PlayerTeam getPlayerTeam(ServerPlayerEntity player) {
        PlayerTeam team = teamManager.getPlayerTeam(player);
        EventMod.LOGGER.trace("{}Получение команды для игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), team);
        return team;
    }

    /**
     * Устанавливает активную карточку игрока.
     * @param player игрок
     * @param cardId ID карточки
     */
    public static void setActiveCard(ServerPlayerEntity player, String cardId) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();
        PlayerClass pc = getPlayerClass(player);

        if (pc == null) {
            EventMod.LOGGER.warn("{}Игрок {} не имеет класса, невозможно установить карточку",
                    LOG_PREFIX, playerName);
            return;
        }

        EventMod.LOGGER.info("{}Установка активной карточки для игрока {}: {} (класс: {})",
                LOG_PREFIX, playerName, cardId, pc);

        // Сохраняем карточку
        cardManager.setActiveCard(uuid, cardId);

        // Активируем её эффекты
        effectManager.activateCardEffects(player, cardId, pc);
    }

    /**
     * Получает активную карточку игрока.
     * @param player игрок
     * @return ID активной карточки или null
     */
    public static String getActiveCard(ServerPlayerEntity player) {
        String cardId = cardManager.getActiveCard(player.getUuid());
        EventMod.LOGGER.trace("{}Получение активной карточки для игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), cardId);
        return cardId;
    }

    /**
     * Получает все активные эффекты игрока.
     * @param player игрок
     * @return множество ID эффектов
     */
    public static Set<String> getAllActiveEffects(ServerPlayerEntity player) {
        Set<String> effects = effectManager.getAllActiveEffects(player.getUuid());
        EventMod.LOGGER.trace("{}Получение активных эффектов для игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), effects.size());
        return effects;
    }

    /**
     * Проверяет наличие эффекта у игрока.
     * @param player игрок
     * @param effectId ID эффекта
     * @return true если эффект активен
     */
    public static boolean hasEffect(ServerPlayerEntity player, String effectId) {
        boolean hasEffect = effectManager.hasEffect(player.getUuid(), effectId);
        EventMod.LOGGER.trace("{}Проверка эффекта {} у игрока {}: {}",
                LOG_PREFIX, effectId, player.getName().getString(), hasEffect);
        return hasEffect;
    }

    /**
     * Проверяет, имеет ли игрок снижение урона от падения.
     * @param player игрок
     * @return true если эффект активен
     */
    public static boolean hasFallDamageReduction(ServerPlayerEntity player) {
        return hasEffect(player, "fall_damage_reduction");
    }

    /**
     * Проверяет, активен ли эффект отталкивания щитом.
     * @param player игрок
     * @return true если эффект активен
     */
    public static boolean isShieldKnockbackActive(ServerPlayerEntity player) {
        return hasEffect(player, "shield_knockback");
    }

    /**
     * Отмечает цель как пораженную ассасином.
     * @param assassin ассасин
     * @param target цель
     */
    public static void markTargetAsHit(ServerPlayerEntity assassin, ServerPlayerEntity target) {
        EventMod.LOGGER.debug("{}Метка цели {} как пораженной ассасином {}",
                LOG_PREFIX, target.getName().getString(), assassin.getName().getString());
        assassinManager.markTargetAsHit(assassin.getUuid(), target.getUuid());
    }

    /**
     * Отмечает цель как пораженную с таймаутом.
     * @param assassin ассасин
     * @param target цель
     * @param timeoutSeconds таймаут в секундах
     */
    public static void markTargetAsHitWithTimeout(ServerPlayerEntity assassin, ServerPlayerEntity target,
                                                  long timeoutSeconds) {
        EventMod.LOGGER.debug("{}Метка цели {} как пораженной ассасином {} с таймаутом {} сек",
                LOG_PREFIX, target.getName().getString(), assassin.getName().getString(), timeoutSeconds);
        assassinManager.markTargetAsHitWithTimeout(assassin.getUuid(), target.getUuid(), timeoutSeconds);
    }

    /**
     * Проверяет, атаковал ли уже ассасин эту цель.
     * @param assassin ассасин
     * @param target цель
     * @return true если цель уже была атакована
     */
    public static boolean hasAlreadyHitTarget(ServerPlayerEntity assassin, ServerPlayerEntity target) {
        boolean hasHit = assassinManager.hasAlreadyHitTarget(assassin.getUuid(), target.getUuid());
        EventMod.LOGGER.trace("{}Проверка, атаковал ли {} цель {}: {}",
                LOG_PREFIX, assassin.getName().getString(), target.getName().getString(), hasHit);
        return hasHit;
    }

    /**
     * Сбрасывает цели ассасина.
     * @param assassin ассасин
     */
    public static void resetAssassinTargets(ServerPlayerEntity assassin) {
        EventMod.LOGGER.debug("{}Сброс целей для ассасина {}",
                LOG_PREFIX, assassin.getName().getString());
        assassinManager.resetAssassinTargets(assassin.getUuid());
    }

    /**
     * Сбрасывает все эффекты игрока.
     * @param player игрок
     */
    public static void resetAllEffects(ServerPlayerEntity player) {
        EventMod.LOGGER.info("{}Сброс всех эффектов для игрока {}",
                LOG_PREFIX, player.getName().getString());
        effectManager.resetAllEffects(player);
    }

    /**
     * Очищает все эффекты карточек игрока.
     * @param player игрок
     */
    public static void clearAllCardEffects(ServerPlayerEntity player) {
        EventMod.LOGGER.info("{}Очистка всех эффектов карточек для игрока {}",
                LOG_PREFIX, player.getName().getString());
        effectManager.clearAllCardEffects(player);
    }

    /**
     * Обрабатывает смерть игрока.
     * @param player умерший игрок
     */
    public static void onPlayerDeath(ServerPlayerEntity player) {
        EventMod.LOGGER.info("{}Сохранение данных при смерти игрока: {}",
                LOG_PREFIX, player.getName().getString());

        UUID uuid = player.getUuid();
        PlayerDeathCacheManager.DeathData deathData = new PlayerDeathCacheManager.DeathData(
                classManager.getPlayerClass(uuid),
                teamManager.getPlayerTeam(uuid),
                cardManager.getActiveCard(uuid),
                effectManager.getActiveEffects(uuid),
                effectManager.getHealthModifier(uuid)
        );

        deathCacheManager.saveDeathData(uuid, deathData);
    }

    /**
     * Обрабатывает возрождение игрока.
     * @param player возрожденный игрок
     */
    public static void onPlayerRespawn(ServerPlayerEntity player) {
        EventMod.LOGGER.info("{}Восстановление данных при возрождении игрока: {}",
                LOG_PREFIX, player.getName().getString());

        UUID uuid = player.getUuid();
        PlayerDeathCacheManager.DeathData deathData = deathCacheManager.getDeathData(uuid);

        if (deathData != null) {
            EventMod.LOGGER.debug("{}Найдены сохраненные данные для игрока {}",
                    LOG_PREFIX, player.getName().getString());

            // Восстанавливаем данные
            if (deathData.playerClass() != null) {
                classManager.setPlayerClass(uuid, deathData.playerClass());
            }
            if (deathData.team() != null && deathData.team() != PlayerTeam.NONE) {
                teamManager.setPlayerTeam(uuid, deathData.team());
            }
            if (deathData.activeCard() != null) {
                cardManager.setActiveCard(uuid, deathData.activeCard());
            }

            // Восстанавливаем эффекты
            effectManager.restoreEffects(player, deathData);

            // Очищаем кэш
            deathCacheManager.removeDeathData(uuid);
        } else {
            EventMod.LOGGER.warn("{}Не найдены сохраненные данные для игрока {}",
                    LOG_PREFIX, player.getName().getString());
        }
    }

    /**
     * Удаляет все данные игрока.
     * @param uuid UUID игрока
     */
    public static void removePlayerData(UUID uuid) {
        EventMod.LOGGER.info("{}Удаление всех данных для UUID: {}", LOG_PREFIX, uuid);

        classManager.removePlayerClass(uuid);
        teamManager.removePlayerTeam(uuid);
        cardManager.removeActiveCard(uuid);
        effectManager.removePlayerEffects(uuid);
        assassinManager.removeAssassinData(uuid);
        deathCacheManager.removeDeathData(uuid);

        EventMod.LOGGER.debug("{}Все данные для UUID {} удалены", LOG_PREFIX, uuid);
    }

    /**
     * Получает статистику использования менеджеров.
     * @return строка со статистикой
     */
    public static String getStats() {
        return String.format("PlayerDataManager Stats: Classes=%d, Teams=%d, Cards=%d",
                classManager.getPlayerCount(),
                0, // teamManager не имеет метода getPlayerCount
                cardManager.getPlayerCount());
    }
}