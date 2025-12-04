package ru.rebey.eventmod.data;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.effect.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер для управления эффектами игроков.
 * Обрабатывает активацию, деактивацию и хранение эффектов карточек.
 */
public class PlayerEffectManager {
    private static final String LOG_PREFIX = "[PlayerEffectManager] ";
    private static final String HEALTH_MODIFIER_ID = "eventmod:extra_health";

    // Хранилища данных
    private final Map<UUID, Set<String>> activeEffects = new ConcurrentHashMap<>();
    private final Map<UUID, Double> healthModifiers = new ConcurrentHashMap<>();

    // Карта эффектов для карточек
    private static final Map<String, List<String>> CARD_EFFECTS_MAP = createCardEffectsMap();

    /**
     * Создает карту соответствия карточек и их эффектов.
     * @return карта эффектов для каждой карточки
     */
    private static Map<String, List<String>> createCardEffectsMap() {
        EventMod.LOGGER.info("{}Создание карты эффектов карточек", LOG_PREFIX);

        Map<String, List<String>> map = new HashMap<>();

        // Танк карточки
        map.put("tank_card_1", Arrays.asList("extra_health_8", "tank_slowness"));
        map.put("tank_card_2", Arrays.asList("fall_damage_reduction"));
        map.put("tank_card_3", Arrays.asList("shield_knockback", "hunger_drain"));

        // Ассасин карточки
        map.put("assassin_card_1", Arrays.asList("assassin_strength", "reduced_health_4"));
        map.put("assassin_card_2", Arrays.asList("stealth_effect", "leather_armor_only"));
        map.put("assassin_card_3", Arrays.asList("increased_speed", "fire_inventory_destruction"));

        EventMod.LOGGER.debug("{}Создана карта для {} карточек", LOG_PREFIX, map.size());
        return map;
    }

    /**
     * Активирует эффекты карточки для игрока.
     * @param player игрок
     * @param cardId ID карточки
     * @param playerClass класс игрока
     */
    public void activateCardEffects(ServerPlayerEntity player, String cardId, PlayerClass playerClass) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        EventMod.LOGGER.info("{}Активация эффектов карточки для игрока {}: {} (класс: {})",
                LOG_PREFIX, playerName, cardId, playerClass);

        List<String> effects = CARD_EFFECTS_MAP.get(cardId);
        if (effects == null) {
            EventMod.LOGGER.warn("{}Неизвестный ID карточки: {}", LOG_PREFIX, cardId);
            return;
        }

        // Получаем текущие эффекты игрока
        Set<String> playerEffects = activeEffects.computeIfAbsent(uuid, k -> new HashSet<>());
        EventMod.LOGGER.debug("{}Текущие эффекты игрока {}: {}", LOG_PREFIX, playerName, playerEffects);

        // Добавляем новые эффекты
        int addedCount = 0;
        for (String effectId : effects) {
            if (playerEffects.add(effectId)) {
                EventMod.LOGGER.info("{}  Добавлен эффект: {} для игрока {}",
                        LOG_PREFIX, effectId, playerName);
                applyEffectById(player, effectId, playerClass);
                addedCount++;
            } else {
                EventMod.LOGGER.debug("{}  Эффект {} уже активен у игрока {}",
                        LOG_PREFIX, effectId, playerName);
            }
        }

        EventMod.LOGGER.info("{}Всего эффектов для игрока {}: {} (добавлено: {})",
                LOG_PREFIX, playerName, playerEffects.size(), addedCount);
    }

    /**
     * Применяет эффект по его ID.
     * @param player игрок
     * @param effectId ID эффекта
     * @param playerClass класс игрока
     */
    private void applyEffectById(ServerPlayerEntity player, String effectId, PlayerClass playerClass) {
        String playerName = player.getName().getString();

        try {
            EventMod.LOGGER.info("{}  Применение эффекта: {} для игрока {}",
                    LOG_PREFIX, effectId, playerName);

            switch (effectId) {
                case "extra_health_8":
                    updateHealthModifier(player, 8.0);
                    break;
                case "tank_slowness":
                    EventMod.LOGGER.info("{}    Активировано замедление танка (-30%)", LOG_PREFIX);
                    break;
                case "fall_damage_reduction":
                    EventMod.LOGGER.info("{}    Активировано снижение урона от падения", LOG_PREFIX);
                    break;
                case "shield_knockback":
                    EventMod.LOGGER.info("{}    Активировано отталкивание щитом", LOG_PREFIX);
                    break;
                case "hunger_drain":
                    EventMod.LOGGER.info("{}    Активирован быстрый голод", LOG_PREFIX);
                    break;
                case "assassin_strength":
                    new AssassinCard1Effect().apply(player);
                    break;
                case "reduced_health_4":
                    updateHealthModifier(player, -4.0);
                    break;
                case "stealth_effect":
                    StealthEffect.apply(player);
                    break;
                case "leather_armor_only":
                    ArmorRestrictionEffect.apply(player);
                    break;
                case "increased_speed":
                    new IncreasedSpeedEffect().apply(player);
                    break;
                case "fire_inventory_destruction":
                    FireInventoryDestructionEffect.apply(player);
                    break;
                default:
                    EventMod.LOGGER.warn("{}    Неизвестный эффект: {}", LOG_PREFIX, effectId);
            }

            EventMod.LOGGER.debug("{}  Эффект {} успешно применен для игрока {}",
                    LOG_PREFIX, effectId, playerName);
        } catch (Exception e) {
            EventMod.LOGGER.error("{}  Ошибка при применении эффекта {} для игрока {}: {}",
                    LOG_PREFIX, effectId, playerName, e.getMessage(), e);
        }
    }

    /**
     * Обновляет модификатор здоровья игрока.
     * @param player игрок
     * @param amount изменение здоровья (положительное или отрицательное)
     */
    private void updateHealthModifier(ServerPlayerEntity player, double amount) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        // Получаем текущий модификатор
        Double currentMod = healthModifiers.getOrDefault(uuid, 0.0);
        Double newMod = currentMod + amount;

        EventMod.LOGGER.info("{}Обновление модификатора здоровья для игрока {}: {} + {} = {}",
                LOG_PREFIX, playerName, currentMod, amount, newMod);

        // Сохраняем новое значение
        healthModifiers.put(uuid, newMod);

        // Применяем суммарный модификатор
        applyHealthModifier(player, newMod);
    }

    /**
     * Применяет суммарный модификатор здоровья игроку.
     * @param player игрок
     * @param totalAmount общий модификатор здоровья
     */
    private void applyHealthModifier(ServerPlayerEntity player, double totalAmount) {
        String playerName = player.getName().getString();

        EventMod.LOGGER.info("{}Применение модификатора здоровья для игрока {}: +{} HP",
                LOG_PREFIX, playerName, totalAmount);

        var attr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (attr != null) {
            // Удаляем старый модификатор
            attr.removeModifier(net.minecraft.util.Identifier.of(HEALTH_MODIFIER_ID));

            // Добавляем новый суммарный модификатор
            var modifier = new EntityAttributeModifier(
                    net.minecraft.util.Identifier.of(HEALTH_MODIFIER_ID),
                    totalAmount,
                    EntityAttributeModifier.Operation.ADD_VALUE
            );
            attr.addPersistentModifier(modifier);

            EventMod.LOGGER.info("{}  Добавлен модификатор здоровья: +{} HP", LOG_PREFIX, totalAmount);

            // Обновляем текущее здоровье
            float currentHealth = player.getHealth();
            float maxHealth = player.getMaxHealth();

            if (currentHealth > maxHealth) {
                player.setHealth(maxHealth);
                EventMod.LOGGER.debug("{}  Здоровье скорректировано: {} -> {}",
                        LOG_PREFIX, currentHealth, maxHealth);
            }

            EventMod.LOGGER.debug("{}  Текущее здоровье игрока {}: {}/{}",
                    LOG_PREFIX, playerName, player.getHealth(), maxHealth);
        } else {
            EventMod.LOGGER.error("{}  Не удалось получить атрибут здоровья", LOG_PREFIX);
        }
    }

    /**
     * Восстанавливает эффекты после смерти игрока.
     * @param player игрок
     * @param deathData данные смерти
     */
    public void restoreEffects(ServerPlayerEntity player, PlayerDeathCacheManager.DeathData deathData) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        EventMod.LOGGER.info("{}Восстановление эффектов для игрока {} после смерти",
                LOG_PREFIX, playerName);

        // Очищаем текущие эффекты перед восстановлением
        int removedCount = activeEffects.containsKey(uuid) ? activeEffects.get(uuid).size() : 0;
        activeEffects.remove(uuid);
        resetHealthModifier(player);

        // Восстанавливаем модификатор здоровья
        if (deathData.healthModifier() != null) {
            healthModifiers.put(uuid, deathData.healthModifier());
            EventMod.LOGGER.info("{}Восстановлен модификатор здоровья: +{} HP",
                    LOG_PREFIX, deathData.healthModifier());
            applyHealthModifier(player, deathData.healthModifier());
        }

        // Восстанавливаем активные эффекты
        if (!deathData.activeEffects().isEmpty()) {
            Set<String> effects = new HashSet<>(deathData.activeEffects());
            activeEffects.put(uuid, effects);

            EventMod.LOGGER.info("{}Восстановлено эффектов: {} (удалено: {})",
                    LOG_PREFIX, effects.size(), removedCount);
            EventMod.LOGGER.debug("{}Восстановленные эффекты: {}", LOG_PREFIX, effects);

            // Применяем эффекты (кроме health, который уже применен)
            int appliedCount = 0;
            for (String effectId : effects) {
                if (!effectId.equals("extra_health_8") && !effectId.equals("reduced_health_4")) {
                    applyRestoredEffectById(player, effectId, deathData.playerClass());
                    appliedCount++;
                }
            }

            EventMod.LOGGER.debug("{}Применено восстановленных эффектов: {}", LOG_PREFIX, appliedCount);
        } else {
            EventMod.LOGGER.debug("{}Нет эффектов для восстановления", LOG_PREFIX);
        }
    }

    /**
     * Применяет восстановленный эффект после смерти.
     * @param player игрок
     * @param effectId ID эффекта
     * @param playerClass класс игрока
     */
    private void applyRestoredEffectById(ServerPlayerEntity player, String effectId, PlayerClass playerClass) {
        String playerName = player.getName().getString();

        try {
            EventMod.LOGGER.debug("{}  Восстановление эффекта: {} для игрока {}",
                    LOG_PREFIX, effectId, playerName);

            switch (effectId) {
                case "tank_slowness":
                    EventMod.LOGGER.debug("{}    Восстановлено замедление танка (-30%)", LOG_PREFIX);
                    break;
                case "fall_damage_reduction":
                    EventMod.LOGGER.debug("{}    Восстановлено снижение урона от падения", LOG_PREFIX);
                    break;
                case "shield_knockback":
                    EventMod.LOGGER.debug("{}    Восстановлено отталкивание щитом", LOG_PREFIX);
                    break;
                case "hunger_drain":
                    EventMod.LOGGER.debug("{}    Восстановлен быстрый голод", LOG_PREFIX);
                    break;
                case "assassin_strength":
                    new AssassinCard1Effect().apply(player);
                    break;
                case "stealth_effect":
                    StealthEffect.apply(player);
                    break;
                case "leather_armor_only":
                    ArmorRestrictionEffect.apply(player);
                    break;
                case "increased_speed":
                    new IncreasedSpeedEffect().apply(player);
                    break;
                case "fire_inventory_destruction":
                    FireInventoryDestructionEffect.apply(player);
                    break;
                default:
                    EventMod.LOGGER.warn("{}    Неизвестный эффект при восстановлении: {}",
                            LOG_PREFIX, effectId);
            }

            EventMod.LOGGER.trace("{}  Эффект {} восстановлен для игрока {}",
                    LOG_PREFIX, effectId, playerName);
        } catch (Exception e) {
            EventMod.LOGGER.error("{}  Ошибка при восстановлении эффекта {} для игрока {}: {}",
                    LOG_PREFIX, effectId, playerName, e.getMessage(), e);
        }
    }

    /**
     * Сбрасывает все эффекты игрока.
     * @param player игрок
     */
    public void resetAllEffects(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        EventMod.LOGGER.info("{}Сброс всех эффектов для игрока {}", LOG_PREFIX, playerName);

        // Логируем текущие эффекты перед очисткой
        Set<String> currentEffects = activeEffects.getOrDefault(uuid, new HashSet<>());
        EventMod.LOGGER.debug("{}  Текущие эффекты перед сбросом: {} (количество: {})",
                LOG_PREFIX, currentEffects, currentEffects.size());

        // Очищаем эффекты
        activeEffects.remove(uuid);

        // Логируем эффекты после очистки
        Set<String> afterEffects = activeEffects.getOrDefault(uuid, new HashSet<>());
        EventMod.LOGGER.debug("{}  Эффекты после сброса: {} (количество: {})",
                LOG_PREFIX, afterEffects, afterEffects.size());

        // Сбрасываем модификатор здоровья
        healthModifiers.remove(uuid);
        resetHealthModifier(player);

        // Очищаем статус-эффекты
        player.clearStatusEffects();

        // Снимаем эффект ограничения брони (если есть)
        if (currentEffects.contains("leather_armor_only")) {
            ArmorRestrictionEffect.remove(player);
        }

        EventMod.LOGGER.info("{}  Все эффекты сброшены для игрока {}", LOG_PREFIX, playerName);
    }

    /**
     * Очищает все эффекты карточек игрока.
     * @param player игрок
     */
    public void clearAllCardEffects(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        EventMod.LOGGER.info("{}Очистка всех эффектов карточек для игрока {}", LOG_PREFIX, playerName);

        // Снимаем эффект ограничения брони (если есть)
        Set<String> effects = activeEffects.getOrDefault(uuid, new HashSet<>());
        if (effects.contains("leather_armor_only")) {
            ArmorRestrictionEffect.remove(player);
        }

        int effectCount = effects.size();
        activeEffects.remove(uuid);
        healthModifiers.remove(uuid);

        resetHealthModifier(player);
        player.clearStatusEffects();

        EventMod.LOGGER.debug("{}  Удалено эффектов: {}", LOG_PREFIX, effectCount);
    }

    /**
     * Сбрасывает модификатор здоровья игрока.
     * @param player игрок
     */
    private void resetHealthModifier(ServerPlayerEntity player) {
        String playerName = player.getName().getString();

        EventMod.LOGGER.debug("{}Сброс модификатора здоровья для игрока {}", LOG_PREFIX, playerName);

        var attr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (attr != null) {
            attr.removeModifier(net.minecraft.util.Identifier.of(HEALTH_MODIFIER_ID));
            player.setHealth(player.getHealth());
            EventMod.LOGGER.debug("{}  Модификатор здоровья сброшен", LOG_PREFIX);
        } else {
            EventMod.LOGGER.warn("{}  Не удалось получить атрибут здоровья", LOG_PREFIX);
        }
    }

    // === Геттеры ===

    /**
     * Получает все активные эффекты игрока.
     * @param uuid UUID игрока
     * @return множество ID эффектов
     */
    public Set<String> getAllActiveEffects(UUID uuid) {
        Set<String> effects = activeEffects.getOrDefault(uuid, new HashSet<>());
        EventMod.LOGGER.trace("{}Получение всех эффектов для UUID {}: {}",
                LOG_PREFIX, uuid, effects.size());
        return effects;
    }

    /**
     * Получает активные эффекты игрока.
     * @param uuid UUID игрока
     * @return множество ID эффектов
     */
    public Set<String> getActiveEffects(UUID uuid) {
        return activeEffects.getOrDefault(uuid, new HashSet<>());
    }

    /**
     * Проверяет наличие эффекта у игрока.
     * @param uuid UUID игрока
     * @param effectId ID эффекта
     * @return true если эффект активен
     */
    public boolean hasEffect(UUID uuid, String effectId) {
        Set<String> effects = activeEffects.get(uuid);
        boolean hasEffect = effects != null && effects.contains(effectId);
        EventMod.LOGGER.trace("{}Проверка эффекта {} для UUID {}: {}",
                LOG_PREFIX, effectId, uuid, hasEffect);
        return hasEffect;
    }

    /**
     * Получает модификатор здоровья игрока.
     * @param uuid UUID игрока
     * @return модификатор здоровья или null
     */
    public Double getHealthModifier(UUID uuid) {
        return healthModifiers.get(uuid);
    }

    /**
     * Удаляет все эффекты игрока.
     * @param uuid UUID игрока
     */
    public void removePlayerEffects(UUID uuid) {
        EventMod.LOGGER.info("{}Удаление всех эффектов для UUID {}", LOG_PREFIX, uuid);

        // Снимаем эффект ограничения брони (если есть)
        Set<String> effects = activeEffects.getOrDefault(uuid, new HashSet<>());
        if (effects.contains("leather_armor_only")) {
            ServerPlayerEntity player = findPlayerByUuid(uuid);
            if (player != null) {
                ArmorRestrictionEffect.remove(player);
            }
        }

        int effectCount = effects.size();
        activeEffects.remove(uuid);
        healthModifiers.remove(uuid);

        EventMod.LOGGER.debug("{}  Удалено эффектов: {}", LOG_PREFIX, effectCount);
    }

    /**
     * Находит игрока по UUID.
     * @param uuid UUID игрока
     * @return игрок или null если не найден
     */
    private ServerPlayerEntity findPlayerByUuid(UUID uuid) {
        // Этот метод нужно реализовать через доступ к серверу
        // В текущем контексте PlayerEffectManager нет доступа к серверу
        EventMod.LOGGER.trace("{}Поиск игрока по UUID: {}", LOG_PREFIX, uuid);
        return null;
    }

    /**
     * Получает статистику по эффектам.
     * @return строка со статистикой
     */
    public String getStats() {
        return String.format("PlayerEffectManager Stats: ActivePlayers=%d, TotalEffects=%d",
                activeEffects.size(),
                activeEffects.values().stream().mapToInt(Set::size).sum());
    }
}