// src/main/java/ru/rebey/eventmod/effect/SlowFallingEffect.java
package ru.rebey.eventmod.effect;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.EventMod;

public class SlowFallingEffect implements CardEffect {
    private final int duration; // в тиках; -1 = бесконечно

    public SlowFallingEffect() {
        this(-1);
    }

    public SlowFallingEffect(int duration) {
        this.duration = duration;
    }

    @Override
    public void apply(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, duration, 0, false, false));
        EventMod.LOGGER.debug("Applied Slow Falling to {}", player.getName().getString());
    }
}