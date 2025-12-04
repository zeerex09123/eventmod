package ru.rebey.eventmod.team;

import ru.rebey.eventmod.EventMod;

/**
 * Перечисление команд игроков.
 * Определяет доступные команды для игроков.
 */
public enum PlayerTeam {
    NONE("none", "❓ Нет команды"),
    RED("red", "🔴 Красные"),
    BLUE("blue", "🔵 Синие");

    private static final String LOG_PREFIX = "[PlayerTeam] ";

    private final String id;
    private final String displayName;

    /**
     * Создает новую команду.
     * @param id уникальный идентификатор команды
     * @param displayName отображаемое имя команды
     */
    PlayerTeam(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
        EventMod.LOGGER.trace("{}Создана команда: {} ({})", LOG_PREFIX, displayName, id);
    }

    /**
     * Получает идентификатор команды.
     * @return идентификатор команды
     */
    public String getId() {
        return id;
    }

    /**
     * Получает отображаемое имя команды.
     * @return отображаемое имя
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Получает цвет команды в формате RGB.
     * @return цвет в формате 0xRRGGBB
     */
    public int getColorRGB() {
        switch (this) {
            case RED:
                return 0xFF5555; // Красный
            case BLUE:
                return 0x5555FF; // Синий
            default:
                return 0xFFFFFF; // Белый
        }
    }

    /**
     * Получает цвет команды в формате Minecraft форматирования.
     * @return форматирование команды
     */
    public net.minecraft.util.Formatting getMinecraftFormatting() {
        switch (this) {
            case RED:
                return net.minecraft.util.Formatting.RED;
            case BLUE:
                return net.minecraft.util.Formatting.BLUE;
            default:
                return net.minecraft.util.Formatting.WHITE;
        }
    }

    /**
     * Получает случайную команду (кроме NONE).
     * @return случайная команда (RED или BLUE)
     */
    public static PlayerTeam getRandomTeam() {
        PlayerTeam[] teams = {RED, BLUE};
        PlayerTeam team = teams[(int) (Math.random() * teams.length)];

        EventMod.LOGGER.trace("{}Случайно выбрана команда: {}", LOG_PREFIX, team);
        return team;
    }

    /**
     * Преобразует идентификатор в команду.
     * @param id идентификатор команды
     * @return соответствующая команда или NONE если не найдена
     */
    public static PlayerTeam fromId(String id) {
        if (id == null || id.isEmpty()) {
            EventMod.LOGGER.trace("{}Попытка преобразования пустого ID команды", LOG_PREFIX);
            return NONE;
        }

        for (PlayerTeam team : values()) {
            if (team.id.equals(id)) {
                EventMod.LOGGER.trace("{}ID '{}' преобразован в команду: {}", LOG_PREFIX, id, team);
                return team;
            }
        }

        EventMod.LOGGER.warn("{}Неизвестный ID команды: {}", LOG_PREFIX, id);
        return NONE;
    }

    /**
     * Преобразует имя команды в команду.
     * @param name имя команды
     * @return соответствующая команда или NONE если не найдена
     */
    public static PlayerTeam fromName(String name) {
        if (name == null || name.isEmpty()) {
            EventMod.LOGGER.trace("{}Попытка преобразования пустого имени команды", LOG_PREFIX);
            return NONE;
        }

        try {
            PlayerTeam team = valueOf(name.toUpperCase());
            EventMod.LOGGER.trace("{}Имя '{}' преобразовано в команду: {}", LOG_PREFIX, name, team);
            return team;
        } catch (IllegalArgumentException e) {
            EventMod.LOGGER.warn("{}Неизвестное имя команды: {}", LOG_PREFIX, name);
            return NONE;
        }
    }

    /**
     * Проверяет, является ли команда валидной игровой командой.
     * @return true если команда RED или BLUE
     */
    public boolean isGameTeam() {
        boolean isGameTeam = this == RED || this == BLUE;
        EventMod.LOGGER.trace("{}Проверка игровой команды для {}: {}", LOG_PREFIX, this, isGameTeam);
        return isGameTeam;
    }

    /**
     * Получает противоположную команду.
     * @return противоположная команда или NONE если текущая команда NONE
     */
    public PlayerTeam getOppositeTeam() {
        switch (this) {
            case RED:
                EventMod.LOGGER.trace("{}Противоположная команда для RED: BLUE", LOG_PREFIX);
                return BLUE;
            case BLUE:
                EventMod.LOGGER.trace("{}Противоположная команда для BLUE: RED", LOG_PREFIX);
                return RED;
            default:
                EventMod.LOGGER.trace("{}Противоположная команда для {}: NONE", LOG_PREFIX, this);
                return NONE;
        }
    }

    /**
     * Получает все игровые команды.
     * @return массив игровых команд (RED, BLUE)
     */
    public static PlayerTeam[] getGameTeams() {
        return new PlayerTeam[]{RED, BLUE};
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %s)", displayName, id);
    }
}