// src/main/java/ru/rebey/eventmod/effect/CardEffect.java
package ru.rebey.eventmod.effect;

import net.minecraft.server.network.ServerPlayerEntity;

@FunctionalInterface
public interface CardEffect {
    void apply(ServerPlayerEntity player);
}