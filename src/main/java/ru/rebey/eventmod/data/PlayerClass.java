// src/main/java/ru/rebey/eventmod/data/PlayerClass.java
package ru.rebey.eventmod.data;

public enum PlayerClass {
    TANK("tank"),
    ASSASSIN("assassin"),
    ENGINEER("engineer"),
    MAGE("mage"),
    SCOUT("scout");

    private final String id;

    PlayerClass(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static PlayerClass fromId(String id) {
        for (PlayerClass pc : values()) {
            if (pc.id.equals(id)) {
                return pc;
            }
        }
        return null;
    }
}