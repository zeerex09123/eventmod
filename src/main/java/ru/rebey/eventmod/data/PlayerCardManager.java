package ru.rebey.eventmod.data;

import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.EventMod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер для управления активными карточками игроков.
 * Хранит информацию о текущей выбранной карточке для каждого игрока.
 */
public class PlayerCardManager {
    private static final String LOG_PREFIX = "[PlayerCardManager] ";
    private static final Map<UUID, String> ACTIVE_CARDS = new ConcurrentHashMap<>();

    /**
     * Устанавливает активную карточку для игрока.
     * @param uuid UUID игрока
     * @param cardId ID карточки
     */
    public void setActiveCard(UUID uuid, String cardId) {
        if (cardId == null) {
            String removed = ACTIVE_CARDS.remove(uuid);
            EventMod.LOGGER.info("{}Удалена активная карточка для UUID {} (была: {})",
                    LOG_PREFIX, uuid, removed);
        } else {
            ACTIVE_CARDS.put(uuid, cardId);
            EventMod.LOGGER.info("{}Установлена активная карточка для UUID {}: {}",
                    LOG_PREFIX, uuid, cardId);
        }
    }

    /**
     * Получает активную карточку игрока по UUID.
     * @param uuid UUID игрока
     * @return ID активной карточки или null
     */
    public String getActiveCard(UUID uuid) {
        String cardId = ACTIVE_CARDS.get(uuid);
        EventMod.LOGGER.debug("{}Получена активная карточка для UUID {}: {}",
                LOG_PREFIX, uuid, cardId);
        return cardId;
    }

    /**
     * Получает активную карточку игрока.
     * @param player игрок
     * @return ID активной карточки или null
     */
    public String getActiveCard(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        String cardId = ACTIVE_CARDS.get(uuid);
        EventMod.LOGGER.debug("{}Получена активная карточка для игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), cardId);
        return cardId;
    }

    /**
     * Удаляет активную карточку игрока.
     * @param uuid UUID игрока
     */
    public void removeActiveCard(UUID uuid) {
        String removed = ACTIVE_CARDS.remove(uuid);
        if (removed != null) {
            EventMod.LOGGER.info("{}Удалена активная карточка для UUID {}: {}",
                    LOG_PREFIX, uuid, removed);
        }
    }

    /**
     * Получает количество игроков с активными карточками.
     * @return количество игроков
     */
    public int getPlayerCount() {
        return ACTIVE_CARDS.size();
    }
}