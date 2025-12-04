//src/main/java/ru/rebey/eventmod/data/ClientPlayerData.java
package ru.rebey.eventmod.data;

import ru.rebey.eventmod.EventMod;

import java.util.HashSet;
import java.util.Set;

public class ClientPlayerData {
    private static String playerClass = null;
    private static String teamColor = "none";
    private static final Set<String> activeEffects = new HashSet<>();

    public static void setClass(String c) { playerClass = c; }
    public static void setTeamColor(String c) { teamColor = c; }

    // Новые методы для управления эффектами на клиенте
    public static void addEffect(String effectId) {
        activeEffects.add(effectId);
        EventMod.LOGGER.debug("[ClientPlayerData] Added effect: {}", effectId);
    }

    public static void removeEffect(String effectId) {
        activeEffects.remove(effectId);
        EventMod.LOGGER.debug("[ClientPlayerData] Removed effect: {}", effectId);
    }

    public static void clearEffects() {
        activeEffects.clear();
        EventMod.LOGGER.debug("[ClientPlayerData] Cleared all effects");
    }

    public static boolean hasEffect(String effectId) {
        return activeEffects.contains(effectId);
    }

    public static Set<String> getActiveEffects() {
        return new HashSet<>(activeEffects);
    }

    public static PlayerClass getPlayerClass() {
        if (playerClass == null) return null;
        try {
            return PlayerClass.valueOf(playerClass);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getTeamColor() { return teamColor; }
}