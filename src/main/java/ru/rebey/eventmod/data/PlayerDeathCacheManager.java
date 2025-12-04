package ru.rebey.eventmod.data;

import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.team.PlayerTeam;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер кэша данных игрока после смерти.
 * Сохраняет данные игрока при смерти и восстанавливает при возрождении.
 */
public class PlayerDeathCacheManager {
    private static final String LOG_PREFIX = "[PlayerDeathCacheManager] ";
    private static final Map<UUID, DeathData> DEATH_CACHE = new ConcurrentHashMap<>();

    /**
     * Запись данных игрока при смерти.
     * @param playerClass класс игрока
     * @param team команда игрока
     * @param activeCard активная карточка
     * @param activeEffects активные эффекты
     * @param healthModifier модификатор здоровья
     */
    public record DeathData(
            PlayerClass playerClass,
            PlayerTeam team,
            String activeCard,
            Set<String> activeEffects,
            Double healthModifier
    ) {
        @Override
        public String toString() {
            return String.format("DeathData{class=%s, team=%s, card=%s, effects=%s, healthMod=%s}",
                    playerClass, team, activeCard, activeEffects, healthModifier);
        }
    }

    /**
     * Сохраняет данные игрока при смерти.
     * @param uuid UUID игрока
     * @param deathData данные для сохранения
     */
    public void saveDeathData(UUID uuid, DeathData deathData) {
        EventMod.LOGGER.info("{}Сохранение данных смерти для UUID {}: {}",
                LOG_PREFIX, uuid, deathData);
        DEATH_CACHE.put(uuid, deathData);

        EventMod.LOGGER.debug("{}Всего сохраненных записей в кэше: {}",
                LOG_PREFIX, DEATH_CACHE.size());
    }

    /**
     * Получает сохраненные данные игрока.
     * @param uuid UUID игрока
     * @return сохраненные данные или null если не найдены
     */
    public DeathData getDeathData(UUID uuid) {
        DeathData data = DEATH_CACHE.get(uuid);
        if (data != null) {
            EventMod.LOGGER.info("{}Найдены сохраненные данные для UUID {}: {}",
                    LOG_PREFIX, uuid, data);
        } else {
            EventMod.LOGGER.warn("{}Не найдены сохраненные данные для UUID {}",
                    LOG_PREFIX, uuid);
        }
        return data;
    }

    /**
     * Удаляет сохраненные данные игрока.
     * @param uuid UUID игрока
     */
    public void removeDeathData(UUID uuid) {
        DeathData removed = DEATH_CACHE.remove(uuid);
        if (removed != null) {
            EventMod.LOGGER.info("{}Удалены сохраненные данные для UUID {}: {}",
                    LOG_PREFIX, uuid, removed);
        } else {
            EventMod.LOGGER.debug("{}Нет данных для удаления UUID {}", LOG_PREFIX, uuid);
        }
    }

    /**
     * Очищает весь кэш данных смерти.
     */
    public void clearCache() {
        int size = DEATH_CACHE.size();
        DEATH_CACHE.clear();
        EventMod.LOGGER.info("{}Кэш данных смерти очищен. Удалено записей: {}",
                LOG_PREFIX, size);
    }

    /**
     * Получает количество сохраненных записей в кэше.
     * @return количество записей
     */
    public int getCacheSize() {
        return DEATH_CACHE.size();
    }

    /**
     * Проверяет, есть ли сохраненные данные для игрока.
     * @param uuid UUID игрока
     * @return true если данные есть
     */
    public boolean hasDeathData(UUID uuid) {
        return DEATH_CACHE.containsKey(uuid);
    }
}