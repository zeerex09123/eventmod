package ru.rebey.eventmod.data;

import ru.rebey.eventmod.network.SyncAllPlayersPayload;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Клиентский класс для хранения списка игроков и их данных.
 * Потокобезопасное хранилище информации об игроках на клиенте.
 */
public class ClientPlayerList {
    private static final java.util.logging.Logger LOGGER =
            java.util.logging.Logger.getLogger(ClientPlayerList.class.getName());

    private static final List<SyncAllPlayersPayload.PlayerInfo> PLAYERS =
            new CopyOnWriteArrayList<>();

    /**
     * Устанавливает список игроков.
     * @param players новый список игроков
     */
    public static void setPlayers(List<SyncAllPlayersPayload.PlayerInfo> players) {
        LOGGER.fine("Установка списка игроков. Новое количество: " + players.size());
        PLAYERS.clear();
        PLAYERS.addAll(players);
        LOGGER.fine("Список игроков обновлен. Всего игроков: " + PLAYERS.size());
    }

    /**
     * Получает неизменяемый список игроков.
     * @return список игроков
     */
    public static List<SyncAllPlayersPayload.PlayerInfo> getPlayers() {
        LOGGER.finest("Получение списка игроков. Всего: " + PLAYERS.size());
        return Collections.unmodifiableList(PLAYERS);
    }
}