// src/main/java/ru/rebey/eventmod/mixin/ServerPlayerEntityMixin.java
package ru.rebey.eventmod.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("ShieldHunger");

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        // Логируем каждый тик (осторожно — много строк!)
        // LOGGER.debug("Tick for player: {}", player.getName().getString());

        if (ru.rebey.eventmod.data.PlayerDataHandler.isShieldKnockbackActive(player)) {
            long worldTime = player.getWorld().getTime();
            if (worldTime % 40 == 0) { // каждые 2 секунды (40 тиков)
                float foodLevel = player.getHungerManager().getFoodLevel();
                float saturation = player.getHungerManager().getSaturationLevel();

                LOGGER.info(
                        "HUNGER DRAIN (20x) for {}: food={}, saturation={}",
                        player.getName().getString(),
                        foodLevel,
                        saturation
                );

                // Увеличенная трата насыщения
                player.getHungerManager().addExhaustion(1.75f);
            }
        }
    }
}