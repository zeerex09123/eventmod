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
import ru.rebey.eventmod.EventMod;

/**
 * Mixin для обработки отталкивания щитом.
 * При атаке щитом отбрасывает врагов с усиленной силой.
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    private static final String LOG_PREFIX = "[LivingEntityMixin] ";
    private static final Logger LOGGER = LoggerFactory.getLogger("ShieldKnockback");

    /**
     * Обрабатывает получение урона для применения отталкивания щитом.
     * @param source источник урона
     * @param amount количество урона
     * @param cir callback для возврата результата
     */
    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        try {
            Entity attacker = source.getAttacker();
            LivingEntity target = (LivingEntity) (Object) this;

            // Логируем все атаки (для отладки)
            EventMod.LOGGER.trace("{}Событие урона: атакующий={}, цель={}, урон={}",
                    LOG_PREFIX,
                    attacker != null ? attacker.getName().getString() : "null",
                    target.getName().getString(),
                    amount);

            if (attacker instanceof ServerPlayerEntity player && target instanceof PlayerEntity) {
                Hand activeHand = player.getActiveHand();
                boolean hasShield = player.getStackInHand(activeHand).isOf(Items.SHIELD);

                EventMod.LOGGER.trace("{}Атакующий имеет щит в активной руке: {}",
                        LOG_PREFIX, hasShield);

                if (hasShield) {
                    // Усиленное отталкивание
                    applyShieldKnockback(player, target);
                }
            }

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при обработке урона щитом: {}",
                    LOG_PREFIX, e.getMessage(), e);
        }
    }

    /**
     * Применяет отталкивание щитом к цели.
     * @param player атакующий игрок со щитом
     * @param target цель отталкивания
     */
    private void applyShieldKnockback(ServerPlayerEntity player, LivingEntity target) {
        try {
            Vec3d attackerPos = player.getPos();
            Vec3d targetPos = target.getPos();
            Vec3d dir = targetPos.subtract(attackerPos).normalize();

            // Увеличиваем силу отталкивания
            double kx = dir.x * 2.5; // Было 1.5 → теперь 2.5
            double kz = dir.z * 2.5;
            double ky = 0.6; // Больше подброса

            target.setVelocity(target.getVelocity().add(kx, ky, kz));

            EventMod.LOGGER.info("{}Применено ОТТАЛКИВАНИЕ ЩИТОМ к {} от {}",
                    LOG_PREFIX, target.getName().getString(), player.getName().getString());
            EventMod.LOGGER.debug("{}  Сила отталкивания: x={}, y={}, z={}",
                    LOG_PREFIX, kx, ky, kz);
            EventMod.LOGGER.debug("{}  Позиция атакующего: x={}, y={}, z={}",
                    LOG_PREFIX, attackerPos.x, attackerPos.y, attackerPos.z);
            EventMod.LOGGER.debug("{}  Позиция цели: x={}, y={}, z={}",
                    LOG_PREFIX, targetPos.x, targetPos.y, targetPos.z);

            LOGGER.info("Applied SHIELD KNOCKBACK to {}", target.getName().getString());

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при применении отталкивания щитом: {}",
                    LOG_PREFIX, e.getMessage(), e);
        }
    }

    /**
     * Проверяет, имеет ли игрок активный эффект отталкивания щитом.
     * @param player игрок для проверки
     * @return true если эффект активен
     */
    private boolean hasShieldKnockbackEffect(ServerPlayerEntity player) {
        boolean hasEffect = ru.rebey.eventmod.data.PlayerDataHandler.isShieldKnockbackActive(player);
        EventMod.LOGGER.trace("{}Проверка эффекта отталкивания щитом для {}: {}",
                LOG_PREFIX, player.getName().getString(), hasEffect);
        return hasEffect;
    }

    /**
     * Логирует детали отталкивания.
     */
    private void logKnockbackDetails(ServerPlayerEntity player, LivingEntity target,
                                     Vec3d direction, Vec3d force) {
        EventMod.LOGGER.debug("{}Детали отталкивания:", LOG_PREFIX);
        EventMod.LOGGER.debug("{}  Атакующий: {}", LOG_PREFIX, player.getName().getString());
        EventMod.LOGGER.debug("{}  Цель: {}", LOG_PREFIX, target.getName().getString());
        EventMod.LOGGER.debug("{}  Направление: x={}, y={}, z={}",
                LOG_PREFIX, direction.x, direction.y, direction.z);
        EventMod.LOGGER.debug("{}  Сила: x={}, y={}, z={}",
                LOG_PREFIX, force.x, force.y, force.z);
        EventMod.LOGGER.debug("{}  Скорость цели до: x={}, y={}, z={}",
                LOG_PREFIX, target.getVelocity().x, target.getVelocity().y, target.getVelocity().z);
    }
}