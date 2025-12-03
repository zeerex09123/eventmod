// src/main/java/ru/rebey/eventmod/team/PlayerTeam.java
package ru.rebey.eventmod.team;

public enum PlayerTeam {
    NONE, RED, BLUE;

    public static PlayerTeam getRandomTeam() {
        return values()[(int) (Math.random() * 2) + 1]; // пропускаем NONE
    }
}