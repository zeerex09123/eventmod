// src/main/java/ru/rebey/eventmod/effect/SlownessEffect.java
package ru.rebey.eventmod.effect;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.EventMod;

public class SlownessEffect implements CardEffect {
    private final int amplifier; // 0 = Slowness I (−15%), 1 = II (−30%)
    private final int duration;  // в тиках; -1 = бесконечно

    public SlownessEffect(int amplifier) {
        this(amplifier, -1);
    }

    public SlownessEffect(int amplifier, int duration) {
        this.amplifier = amplifier;
        this.duration = duration;
    }

    @Override
    public void apply(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, duration, amplifier, false, false));
        EventMod.LOGGER.debug("Applied Slowness {} to {}", amplifier + 1, player.getName().getString());
    }
}