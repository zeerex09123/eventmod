package ru.rebey.eventmod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.data.PlayerDataHandler;

/**
 * Mixin для модификации урона ассасина.
 * Увеличивает урон ассасина по целям с полным здоровьем на 50%.
 */
@Mixin(LivingEntity.class)
public class AssassinDamageMixin {
    private static final String LOG_PREFIX = "[AssassinDamageMixin] ";

    /**
     * Модифицирует урон, наносимый ассасином.
     * @param amount исходный урон
     * @param source источник урона
     * @return модифицированный урон
     */
    @ModifyVariable(
            method = "damage",
            at = @At("HEAD"),
            argsOnly = true
    )
    private float modifyAssassinDamage(float amount, DamageSource source) {
        try {
            LivingEntity target = (LivingEntity) (Object) this;
            Entity attacker = source.getAttacker();

            // Проверяем условия для эффекта ассасина
            if (attacker instanceof ServerPlayerEntity assassin && target instanceof ServerPlayerEntity targetPlayer) {

                EventMod.LOGGER.trace("{}Проверка урона ассасина: {} -> {}",
                        LOG_PREFIX, assassin.getName().getString(), targetPlayer.getName().getString());

                // Используем новую логику проверки
                if (PlayerDataHandler.shouldApplyAssassinBonus(assassin, targetPlayer)) {
                    float modifiedAmount = amount * 1.5f;

                    EventMod.LOGGER.info("{}Атака по цели с полным HP! Урон увеличен в 1.5 раза: {} -> {}",
                            LOG_PREFIX, amount, modifiedAmount);
                    EventMod.LOGGER.debug("{}  Ассасин: {}, Цель: {}, Здоровье цели: {}/{}, Исходный урон: {}",
                            LOG_PREFIX,
                            assassin.getName().getString(),
                            targetPlayer.getName().getString(),
                            targetPlayer.getHealth(),
                            targetPlayer.getMaxHealth(),
                            amount);

                    // Увеличиваем урон в 1.5 раза (50% увеличение)
                    return modifiedAmount;
                } else {
                    EventMod.LOGGER.trace("{}Бонусный урон не применяется: {} -> {}",
                            LOG_PREFIX, assassin.getName().getString(), targetPlayer.getName().getString());
                }
            } else {
                EventMod.LOGGER.trace("{}Условия для бонусного урона не выполнены: attacker={}, target={}",
                        LOG_PREFIX,
                        attacker != null ? attacker.getName().getString() : "null",
                        target.getName().getString());
            }

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при модификации урона ассасина: {}",
                    LOG_PREFIX, e.getMessage(), e);
        }

        return amount;
    }

    /**
     * Вспомогательный метод для логирования деталей урона.
     */
    private void logDamageDetails(ServerPlayerEntity assassin, ServerPlayerEntity target,
                                  float originalDamage, float modifiedDamage) {
        EventMod.LOGGER.debug("{}Детали урона:", LOG_PREFIX);
        EventMod.LOGGER.debug("{}  Ассасин: {}", LOG_PREFIX, assassin.getName().getString());
        EventMod.LOGGER.debug("{}  Цель: {}", LOG_PREFIX, target.getName().getString());
        EventMod.LOGGER.debug("{}  Здоровье цели: {}/{}",
                LOG_PREFIX, target.getHealth(), target.getMaxHealth());
        EventMod.LOGGER.debug("{}  Исходный урон: {}", LOG_PREFIX, originalDamage);
        EventMod.LOGGER.debug("{}  Модифицированный урон: {}", LOG_PREFIX, modifiedDamage);
        EventMod.LOGGER.debug("{}  Множитель: 1.5x", LOG_PREFIX);
    }
}