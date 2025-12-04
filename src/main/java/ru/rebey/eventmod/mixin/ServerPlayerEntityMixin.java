package ru.rebey.eventmod.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.rebey.eventmod.EventMod;

/**
 * Mixin для обработки истощения голода при использовании щита.
 * При активном эффекте щита увеличивает расход голода.
 */
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    private static final String LOG_PREFIX = "[ServerPlayerEntityMixin] ";
    private static final Logger LOGGER = LoggerFactory.getLogger("ShieldHunger");

    /**
     * Обрабатывает каждый тик игрока для применения истощения голода.
     * @param ci callback информация
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        try {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            String playerName = player.getName().getString();

            // Логируем каждый тик (осторожно — много строк!)
            // EventMod.LOGGER.trace("{}Тик для игрока: {}", LOG_PREFIX, playerName);

            if (ru.rebey.eventmod.data.PlayerDataHandler.isShieldKnockbackActive(player)) {
                long worldTime = player.getWorld().getTime();

                // Каждые 2 секунды (40 тиков) увеличиваем истощение
                if (worldTime % 40 == 0) {
                    float foodLevel = player.getHungerManager().getFoodLevel();
                    float saturation = player.getHungerManager().getSaturationLevel();

                    EventMod.LOGGER.debug("{}ИСТОЩЕНИЕ ГОЛОДА (20x) для {}: еда={}, насыщение={}",
                            LOG_PREFIX, playerName, foodLevel, saturation);

                    // Увеличенная трата насыщения
                    player.getHungerManager().addExhaustion(1.0f);

                    EventMod.LOGGER.trace("{}  Добавлено истощение: 1.0", LOG_PREFIX);

                    LOGGER.info(
                            "HUNGER DRAIN (20x) for {}: food={}, saturation={}",
                            playerName,
                            foodLevel,
                            saturation
                    );
                }
            } else {
                EventMod.LOGGER.trace("{}Игрок {} не имеет эффекта истощения голода",
                        LOG_PREFIX, playerName);
            }

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при обработке тика истощения голода: {}",
                    LOG_PREFIX, e.getMessage(), e);
        }
    }

    /**
     * Проверяет, имеет ли игрок эффект отталкивания щитом.
     * @param player игрок для проверки
     * @return true если эффект активен
     */
    private boolean hasShieldHungerEffect(ServerPlayerEntity player) {
        boolean hasEffect = ru.rebey.eventmod.data.PlayerDataHandler.isShieldKnockbackActive(player);
        EventMod.LOGGER.trace("{}Проверка эффекта истощения голода для {}: {}",
                LOG_PREFIX, player.getName().getString(), hasEffect);
        return hasEffect;
    }

    /**
     * Логирует текущее состояние голода игрока.
     */
    private void logHungerStatus(ServerPlayerEntity player) {
        float foodLevel = player.getHungerManager().getFoodLevel();
        float saturation = player.getHungerManager().getSaturationLevel();
        float exhaustion = player.getHungerManager().getExhaustion();

        EventMod.LOGGER.debug("{}Статус голода игрока {}:", LOG_PREFIX, player.getName().getString());
        EventMod.LOGGER.debug("{}  Уровень еды: {}", LOG_PREFIX, foodLevel);
        EventMod.LOGGER.debug("{}  Насыщение: {}", LOG_PREFIX, saturation);
        EventMod.LOGGER.debug("{}  Истощение: {}", LOG_PREFIX, exhaustion);
    }

    /**
     * Рассчитывает множитель истощения на основе активных эффектов.
     * @param player игрок
     * @return множитель истощения
     */
    private float getHungerMultiplier(ServerPlayerEntity player) {
        // Базовая скорость истощения
        float multiplier = 1.0f;

        // Увеличиваем если есть эффект отталкивания щитом
        if (hasShieldHungerEffect(player)) {
            multiplier *= 20.0f; // 20x увеличение
            EventMod.LOGGER.trace("{}Множитель истощения для {}: {}x",
                    LOG_PREFIX, player.getName().getString(), multiplier);
        }

        return multiplier;
    }
}