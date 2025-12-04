package ru.rebey.eventmod.data;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.team.PlayerTeam;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Фасад для обратной совместимости.
 * Все методы делегируются PlayerDataManager.
 * Старый код продолжит работать без изменений.
 */
public class PlayerDataHandler {
    private static final String LOG_PREFIX = "[PlayerDataHandler] ";

    // Регистрация событий смерти/респавна
    static {
        EventMod.LOGGER.info("{}Регистрация событий смерти/респавна", LOG_PREFIX);

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                EventMod.LOGGER.debug("{}Событие COPY_FROM для игрока {} (alive: {})",
                        LOG_PREFIX, oldPlayer.getName().getString(), alive);
                PlayerDataManager.onPlayerDeath(oldPlayer);
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                EventMod.LOGGER.debug("{}Событие AFTER_RESPAWN для игрока {} (alive: {})",
                        LOG_PREFIX, newPlayer.getName().getString(), alive);
                PlayerDataManager.onPlayerRespawn(newPlayer);
            }
        });
    }

    // === Делегированные методы ===

    /**
     * Устанавливает класс игрока.
     * @param player игрок
     * @param playerClass класс игрока
     */
    public static void setPlayerClass(ServerPlayerEntity player, PlayerClass playerClass) {
        EventMod.LOGGER.debug("{}Установка класса для игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), playerClass);
        PlayerDataManager.setPlayerClass(player, playerClass);
    }

    /**
     * Получает класс игрока.
     * @param player игрок
     * @return класс игрока или null
     */
    public static PlayerClass getPlayerClass(ServerPlayerEntity player) {
        PlayerClass pc = PlayerDataManager.getPlayerClass(player);
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
        PlayerDataManager.setPlayerTeam(player, team);
    }

    /**
     * Получает команду игрока.
     * @param player игрок
     * @return команда игрока
     */
    public static PlayerTeam getPlayerTeam(ServerPlayerEntity player) {
        PlayerTeam team = PlayerDataManager.getPlayerTeam(player);
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
        EventMod.LOGGER.debug("{}Установка активной карточки для игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), cardId);
        PlayerDataManager.setActiveCard(player, cardId);
    }

    /**
     * Получает активную карточку игрока.
     * @param player игрок
     * @return ID активной карточки или null
     */
    public static String getActiveCard(ServerPlayerEntity player) {
        String cardId = PlayerDataManager.getActiveCard(player);
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
        Set<String> effects = PlayerDataManager.getAllActiveEffects(player);
        EventMod.LOGGER.trace("{}Получение активных эффектов для игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), effects.size());
        return effects;
    }

    /**
     * Проверяет, имеет ли игрок снижение урона от падения.
     * @param player игрок
     * @return true если эффект активен
     */
    public static boolean hasFallDamageReduction(ServerPlayerEntity player) {
        boolean hasEffect = PlayerDataManager.hasFallDamageReduction(player);
        EventMod.LOGGER.trace("{}Проверка снижения урона от падения для игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), hasEffect);
        return hasEffect;
    }

    /**
     * Проверяет, активен ли эффект отталкивания щитом.
     * @param player игрок
     * @return true если эффект активен
     */
    public static boolean isShieldKnockbackActive(ServerPlayerEntity player) {
        boolean isActive = PlayerDataManager.isShieldKnockbackActive(player);
        EventMod.LOGGER.trace("{}Проверка отталкивания щитом для игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), isActive);
        return isActive;
    }

    /**
     * Отмечает цель как пораженную ассасином.
     * @param assassin ассасин
     * @param target цель
     */
    public static void markTargetAsHit(ServerPlayerEntity assassin, ServerPlayerEntity target) {
        EventMod.LOGGER.debug("{}Метка цели {} как пораженной ассасином {}",
                LOG_PREFIX, target.getName().getString(), assassin.getName().getString());
        PlayerDataManager.markTargetAsHit(assassin, target);
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
        PlayerDataManager.markTargetAsHitWithTimeout(assassin, target, timeoutSeconds);
    }

    /**
     * Проверяет, атаковал ли уже ассасин эту цель.
     * @param assassin ассасин
     * @param target цель
     * @return true если цель уже была атакована
     */
    public static boolean hasAlreadyHitTarget(ServerPlayerEntity assassin, ServerPlayerEntity target) {
        boolean hasHit = PlayerDataManager.hasAlreadyHitTarget(assassin, target);
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
        PlayerDataManager.resetAssassinTargets(assassin);
    }

    /**
     * Сбрасывает все эффекты игрока.
     * @param player игрок
     */
    public static void resetAllEffects(ServerPlayerEntity player) {
        EventMod.LOGGER.debug("{}Сброс всех эффектов для игрока {}",
                LOG_PREFIX, player.getName().getString());
        PlayerDataManager.resetAllEffects(player);
    }

    /**
     * Очищает все эффекты карточек игрока.
     * @param player игрок
     */
    public static void clearAllCardEffects(ServerPlayerEntity player) {
        EventMod.LOGGER.debug("{}Очистка всех эффектов карточек для игрока {}",
                LOG_PREFIX, player.getName().getString());
        PlayerDataManager.clearAllCardEffects(player);
    }

    /**
     * Удаляет все данные игрока.
     * @param uuid UUID игрока
     */
    public static void removePlayerData(UUID uuid) {
        EventMod.LOGGER.debug("{}Удаление всех данных для UUID {}", LOG_PREFIX, uuid);
        PlayerDataManager.removePlayerData(uuid);
    }

    // === НОВЫЙ МЕТОД для AssassinDamageMixin ===

    /**
     * Проверяет, должен ли применяться бонусный урон ассасина.
     * @param assassin атакующий игрок (ассасин)
     * @param target целевой игрок
     * @return true если нужно применить бонусный урон
     */
    public static boolean shouldApplyAssassinBonus(ServerPlayerEntity assassin, ServerPlayerEntity target) {
        // Проверяем, есть ли у ассасина эффект assassin_strength
        if (!PlayerDataManager.getAllActiveEffects(assassin).contains("assassin_strength")) {
            EventMod.LOGGER.trace("{}У ассасина {} нет эффекта assassin_strength",
                    LOG_PREFIX, assassin.getName().getString());
            return false;
        }

        // Проверяем, что цель имеет полное здоровье
        boolean hasFullHealth = target.getHealth() >= target.getMaxHealth();

        if (!hasFullHealth) {
            EventMod.LOGGER.trace("{}Цель {} не имеет полного здоровья: {}/{}",
                    LOG_PREFIX, target.getName().getString(), target.getHealth(), target.getMaxHealth());
            return false;
        }

        EventMod.LOGGER.debug("{}Цель {} имеет полное HP: {}/{}. Применение бонусного урона от ассасина {}",
                LOG_PREFIX, target.getName().getString(), target.getHealth(), target.getMaxHealth(),
                assassin.getName().getString());

        return true;
    }

    // === НОВЫЙ МЕТОД для получения эффектов по UUID ===
    /**
     * Получает эффекты игрока по UUID.
     * Внимание: Этот метод нужно вызывать только на сервере!
     * @param uuid UUID игрока
     * @return множество ID эффектов
     */
    public static Set<String> getEffectsByUuid(UUID uuid) {
        EventMod.LOGGER.warn("{}getEffectsByUuid вызван для UUID: {}. На клиенте возвращается пустой набор.",
                LOG_PREFIX, uuid);
        return Collections.emptySet(); // Возвращаем пустой набор на клиенте
    }
}