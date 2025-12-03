// src/main/java/ru/rebey/eventmod/data/ClientPlayerList.java
package ru.rebey.eventmod.data;

import ru.rebey.eventmod.network.SyncAllPlayersPayload;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientPlayerList {
    private static final List<SyncAllPlayersPayload.PlayerInfo> PLAYERS = new CopyOnWriteArrayList<>();

    public static void setPlayers(List<SyncAllPlayersPayload.PlayerInfo> players) {
        PLAYERS.clear();
        PLAYERS.addAll(players);
    }

    public static List<SyncAllPlayersPayload.PlayerInfo> getPlayers() {
        return Collections.unmodifiableList(PLAYERS);
    }
}