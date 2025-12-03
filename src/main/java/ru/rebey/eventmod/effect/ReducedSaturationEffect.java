// src/main/java/ru/rebey/eventmod/effect/ReducedSaturationEffect.java
package ru.rebey.eventmod.effect;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.EventMod;

public class ReducedSaturationEffect implements CardEffect {
    @Override
    public void apply(ServerPlayerEntity player) {
        // Hunger effect (голод) — ускоряет расход сытости
        // Длительность: бесконечно (-1), уровень: 0 (стандартный)
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, -1, 0, false, true));
        EventMod.LOGGER.info("Applied Hunger effect (2x hunger drain) to {}", player.getName().getString());
    }
}