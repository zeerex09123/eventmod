package ru.rebey.eventmod.data;

import ru.rebey.eventmod.EventMod;

/**
 * Перечисление классов игроков.
 * Определяет доступные классы и их идентификаторы.
 */
public enum PlayerClass {
    TANK("tank", "🛡️ Танк"),
    ASSASSIN("assassin", "🗡️ Убийца"),
    ENGINEER("engineer", "⚙️ Инженер"),
    MAGE("mage", "🔮 Маг"),
    SCOUT("scout", "👁️ Разведчик");

    private final String id;
    private final String displayName;

    /**
     * Создает новый класс игрока.
     * @param id уникальный идентификатор класса
     * @param displayName отображаемое имя класса
     */
    PlayerClass(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    /**
     * Получает идентификатор класса.
     * @return идентификатор класса
     */
    public String getId() {
        return id;
    }

    /**
     * Получает отображаемое имя класса.
     * @return отображаемое имя
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Преобразует идентификатор в класс игрока.
     * @param id идентификатор класса
     * @return соответствующий класс или null если не найден
     */
    public static PlayerClass fromId(String id) {
        if (id == null || id.isEmpty()) {
            EventMod.LOGGER.warn("[PlayerClass] Попытка преобразования пустого ID");
            return null;
        }

        for (PlayerClass pc : values()) {
            if (pc.id.equals(id)) {
                EventMod.LOGGER.debug("[PlayerClass] ID '{}' преобразован в класс: {}", id, pc);
                return pc;
            }
        }

        EventMod.LOGGER.warn("[PlayerClass] Неизвестный ID класса: {}", id);
        return null;
    }

    /**
     * Получает все доступные классы.
     * @return массив всех классов
     */
    public static PlayerClass[] getAllClasses() {
        return values();
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %s)", displayName, id);
    }
}