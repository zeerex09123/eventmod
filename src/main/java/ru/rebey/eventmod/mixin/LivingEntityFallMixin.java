// src/main/java/ru/rebey/eventmod/mixin/LivingEntityFallMixin.java
package ru.rebey.eventmod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFallMixin {

    @ModifyVariable(
            method = "handleFallDamage",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private float reduceFallDistance(float fallDistance) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayerEntity player &&
                ru.rebey.eventmod.data.PlayerDataHandler.hasFallDamageReduction(player)) {
            return fallDistance * 0.5f; // Уменьшаем дистанцию падения в 2 раза
        }
        return fallDistance;
    }
}