// src/main/java/ru/rebey/eventmod/effect/IncreasedSpeedEffect.java
package ru.rebey.eventmod.effect;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.EventMod;

public class IncreasedSpeedEffect implements CardEffect {
    @Override
    public void apply(ServerPlayerEntity player) {
        // Speed I = +20%, Speed II = +40%
        // Используем Speed I с длительностью -1 (бесконечно)
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, -1, 0, false, false));
        EventMod.LOGGER.info("Applied increased speed to {}", player.getName().getString());
    }
}