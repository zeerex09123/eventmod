package ru.rebey.eventmod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.rebey.eventmod.EventMod;

/**
 * Mixin для модификации урона от падения.
 * Уменьшает урон от падения для игроков с соответствующим эффектом.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityFallMixin {
    private static final String LOG_PREFIX = "[LivingEntityFallMixin] ";

    /**
     * Модифицирует дистанцию падения для расчета урона.
     * @param fallDistance исходная дистанция падения
     * @return модифицированная дистанция падения
     */
    @ModifyVariable(
            method = "handleFallDamage",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private float reduceFallDistance(float fallDistance) {
        try {
            LivingEntity self = (LivingEntity) (Object) this;

            // Проверяем, является ли сущность игроком и имеет ли эффект
            if (self instanceof ServerPlayerEntity player &&
                    ru.rebey.eventmod.data.PlayerDataHandler.hasFallDamageReduction(player)) {

                float modifiedDistance = fallDistance * 0.5f; // Уменьшаем дистанцию падения в 2 раза

                EventMod.LOGGER.info("{}Уменьшение урона от падения для игрока {}: дистанция {} -> {} (-50%)",
                        LOG_PREFIX, player.getName().getString(), fallDistance, modifiedDistance);
                EventMod.LOGGER.debug("{}  Игрок: {}, Эффект: снижение урона от падения",
                        LOG_PREFIX, player.getName().getString());

                return modifiedDistance;
            } else {
                EventMod.LOGGER.trace("{}Без изменений: {} (не игрок или нет эффекта)",
                        LOG_PREFIX, fallDistance);
            }

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при модификации дистанции падения: {}",
                    LOG_PREFIX, e.getMessage(), e);
        }

        return fallDistance;
    }

    /**
     * Вспомогательный метод для логирования деталей падения.
     */
    private void logFallDetails(ServerPlayerEntity player, float originalDistance,
                                float modifiedDistance) {
        EventMod.LOGGER.debug("{}Детали падения:", LOG_PREFIX);
        EventMod.LOGGER.debug("{}  Игрок: {}", LOG_PREFIX, player.getName().getString());
        EventMod.LOGGER.debug("{}  Исходная дистанция: {}", LOG_PREFIX, originalDistance);
        EventMod.LOGGER.debug("{}  Модифицированная дистанция: {}", LOG_PREFIX, modifiedDistance);
        EventMod.LOGGER.debug("{}  Множитель: 0.5x (уменьшение на 50%)", LOG_PREFIX);
        EventMod.LOGGER.debug("{}  Позиция игрока: x={}, y={}, z={}",
                LOG_PREFIX, player.getX(), player.getY(), player.getZ());
    }
}