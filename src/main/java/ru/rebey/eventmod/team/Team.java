// src/main/java/ru/rebey/eventmod/team/Team.java
package ru.rebey.eventmod.team;

import java.util.Random;

public enum Team {
    RED,
    BLUE;

    private static final Random RANDOM = new Random();

    public static Team getRandomTeam() {
        return values()[RANDOM.nextInt(values().length)];
    }
}