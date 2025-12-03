// src/main/java/ru/rebey/eventmod/data/ClientPlayerData.java
package ru.rebey.eventmod.data;

public class ClientPlayerData {
    private static String playerClass = null;
    private static String teamColor = "none";

    public static void setClass(String c) { playerClass = c; }
    public static void setTeamColor(String c) { teamColor = c; }
    public static PlayerClass getPlayerClass() {
        if (playerClass == null) return null;
        try {
            return PlayerClass.valueOf(playerClass); // ← должно совпадать с enum
        } catch (Exception e) {
            return null;
        }
    }
    public static String getTeamColor() { return teamColor; }
}