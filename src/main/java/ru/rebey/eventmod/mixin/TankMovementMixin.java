package ru.rebey.eventmod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.rebey.eventmod.EventMod;

/**
 * Mixin для модификации скорости движения танка.
 * Уменьшает скорость движения танка на 30%.
 */
@Mixin(LivingEntity.class)
public abstract class TankMovementMixin extends Entity {
    private static final String LOG_PREFIX = "[TankMovementMixin] ";

    /**
     * Конструктор сущности.
     * @param type тип сущности
     * @param world мир
     */
    public TankMovementMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * Модифицирует скорость движения сущности.
     * @param cir callback для возврата модифицированной скорости
     */
    @Inject(
            method = "getMovementSpeed",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyTankMovementSpeed(CallbackInfoReturnable<Float> cir) {
        try {
            // Проверяем, является ли сущность игроком
            if ((Object)this instanceof PlayerEntity player) {
                EventMod.LOGGER.trace("{}Проверка скорости движения для игрока {}",
                        LOG_PREFIX, player.getName().getString());

                // Проверяем, есть ли у игрока эффект tank_slowness
                // Этот mixin работает только на сервере, так как PlayerDataHandler требует ServerPlayerEntity
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    var effects = ru.rebey.eventmod.data.PlayerDataHandler.getAllActiveEffects(serverPlayer);

                    if (effects != null && effects.contains("tank_slowness")) {
                        float originalSpeed = cir.getReturnValue();
                        float modifiedSpeed = originalSpeed * 0.7f; // Уменьшаем скорость на 30%

                        EventMod.LOGGER.info("{}Скорость движения танка {} изменена: {} -> {} (-30%)",
                                LOG_PREFIX, player.getName().getString(), originalSpeed, modifiedSpeed);
                        EventMod.LOGGER.debug("{}  Игрок: {}, Эффект: tank_slowness",
                                LOG_PREFIX, player.getName().getString());

                        cir.setReturnValue(modifiedSpeed);
                    } else {
                        EventMod.LOGGER.trace("{}Игрок {} не имеет эффекта tank_slowness",
                                LOG_PREFIX, player.getName().getString());
                    }
                } else {
                    EventMod.LOGGER.trace("{}Игрок {} не является ServerPlayerEntity",
                            LOG_PREFIX, player.getName().getString());
                }
            }

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при модификации скорости движения танка {}: {}",
                    LOG_PREFIX, ((PlayerEntity)(Object)this).getName().getString(), e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Вспомогательный метод для логирования деталей скорости.
     */
    private void logSpeedDetails(ServerPlayerEntity player, float originalSpeed, float modifiedSpeed) {
        EventMod.LOGGER.debug("{}Детали скорости движения:", LOG_PREFIX);
        EventMod.LOGGER.debug("{}  Игрок: {}", LOG_PREFIX, player.getName().getString());
        EventMod.LOGGER.debug("{}  Исходная скорость: {}", LOG_PREFIX, originalSpeed);
        EventMod.LOGGER.debug("{}  Модифицированная скорость: {}", LOG_PREFIX, modifiedSpeed);
        EventMod.LOGGER.debug("{}  Изменение: {}%", LOG_PREFIX,
                Math.round((1 - modifiedSpeed / originalSpeed) * 100));
        EventMod.LOGGER.debug("{}  Позиция игрока: x={}, y={}, z={}",
                LOG_PREFIX, player.getX(), player.getY(), player.getZ());
    }

    /**
     * Проверяет, имеет ли игрок эффект замедления танка.
     * @param player игрок для проверки
     * @return true если эффект активен
     */
    private boolean hasTankSlownessEffect(ServerPlayerEntity player) {
        var effects = ru.rebey.eventmod.data.PlayerDataHandler.getAllActiveEffects(player);
        boolean hasEffect = effects != null && effects.contains("tank_slowness");

        EventMod.LOGGER.trace("{}Проверка эффекта tank_slowness для {}: {}",
                LOG_PREFIX, player.getName().getString(), hasEffect);

        return hasEffect;
    }
}