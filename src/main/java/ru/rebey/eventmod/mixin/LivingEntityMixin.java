// src/main/java/ru/rebey/eventmod/mixin/LivingEntityMixin.java
package ru.rebey.eventmod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("ShieldKnockback");

    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity attacker = source.getAttacker();
        LivingEntity target = (LivingEntity) (Object) this;

        // Логируем все атаки (для отладки)
        LOGGER.debug("Damage event: attacker={}, target={}, amount={}",
                attacker != null ? attacker.getName().getString() : "null",
                target.getName().getString(),
                amount
        );

        if (attacker instanceof ServerPlayerEntity player && target instanceof PlayerEntity) {
            Hand activeHand = player.getActiveHand();
            boolean hasShield = player.getStackInHand(activeHand).isOf(Items.SHIELD);

            LOGGER.debug("Attacker has shield in active hand: {}", hasShield);

            if (hasShield) {
                // Усиленное отталкивание
                Vec3d attackerPos = player.getPos();
                Vec3d targetPos = target.getPos();
                Vec3d dir = targetPos.subtract(attackerPos).normalize();

                // Увеличиваем силу отталкивания
                double kx = dir.x * 2.5; // Было 1.5 → теперь 2.5
                double kz = dir.z * 2.5;
                double ky = 0.6; // Больше подброса

                target.setVelocity(target.getVelocity().add(kx, ky, kz));
                LOGGER.info("Applied SHIELD KNOCKBACK to {}", target.getName().getString());
            }
        }
    }
}