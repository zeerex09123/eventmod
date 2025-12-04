package ru.rebey.eventmod.effect;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import ru.rebey.eventmod.EventMod;

/**
 * Эффект первой карточки ассасина.
 * Добавляет бонусный урон по целям с полным здоровьем, но снижает максимальное здоровье.
 */
public class AssassinCard1Effect implements CardEffect {
    private static final String LOG_PREFIX = "[AssassinCard1Effect] ";
    private static final Identifier HEALTH_MODIFIER_ID = Identifier.of("eventmod", "assassin_health_reduction");

    /**
     * Применяет эффект карточки ассасина.
     * @param player игрок для применения эффекта
     */
    @Override
    public void apply(ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        EventMod.LOGGER.info("{}Применение эффекта карточки ассасина для игрока {}",
                LOG_PREFIX, playerName);

        try {
            // Дебафф: −20% здоровья → −4 HP (от базовых 20)
            var healthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            if (healthAttr != null) {
                // Удаляем старый модификатор (на случай повторного применения)
                healthAttr.removeModifier(HEALTH_MODIFIER_ID);
                EventMod.LOGGER.debug("{}  Удален старый модификатор здоровья", LOG_PREFIX);

                // Добавляем новый: −4.0 HP
                var modifier = new EntityAttributeModifier(
                        HEALTH_MODIFIER_ID,
                        -4.0,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                healthAttr.addPersistentModifier(modifier);

                // Обновляем текущее здоровье
                float oldHealth = player.getHealth();
                player.setHealth(player.getHealth());

                EventMod.LOGGER.info("{}  Добавлен модификатор здоровья: -4.0 HP", LOG_PREFIX);
                EventMod.LOGGER.debug("{}  Здоровье игрока {}: {} -> {}",
                        LOG_PREFIX, playerName, oldHealth, player.getHealth());
            } else {
                EventMod.LOGGER.error("{}  Не удалось получить атрибут здоровья у игрока {}",
                        LOG_PREFIX, playerName);
            }

            // Устанавливаем флаг для миксина
            EventMod.LOGGER.info("{}  Активирован эффект 'Урон по полному HP' для игрока {}",
                    LOG_PREFIX, playerName);

        } catch (Exception e) {
            EventMod.LOGGER.error("{}  Ошибка при применении эффекта ассасина для игрока {}: {}",
                    LOG_PREFIX, playerName, e.getMessage(), e);
        }
    }

    /**
     * Удаляет эффект карточки ассасина.
     * @param player игрок для удаления эффекта
     */
    public void remove(ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        EventMod.LOGGER.info("{}Удаление эффекта карточки ассасина для игрока {}",
                LOG_PREFIX, playerName);

        try {
            var healthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            if (healthAttr != null) {
                healthAttr.removeModifier(HEALTH_MODIFIER_ID);
                player.setHealth(player.getHealth());

                EventMod.LOGGER.info("{}  Удален модификатор здоровья", LOG_PREFIX);
                EventMod.LOGGER.debug("{}  Здоровье игрока {}: {}", LOG_PREFIX, playerName, player.getHealth());
            }
        } catch (Exception e) {
            EventMod.LOGGER.error("{}  Ошибка при удалении эффекта ассасина для игрока {}: {}",
                    LOG_PREFIX, playerName, e.getMessage(), e);
        }
    }

    /**
     * Проверяет, применен ли эффект к игроку.
     * @param player игрок для проверки
     * @return true если эффект активен
     */
    public static boolean isApplied(ServerPlayerEntity player) {
        var healthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            boolean hasModifier = healthAttr.getModifier(HEALTH_MODIFIER_ID) != null;
            EventMod.LOGGER.trace("{}Проверка эффекта ассасина для игрока {}: {}",
                    LOG_PREFIX, player.getName().getString(), hasModifier);
            return hasModifier;
        }
        return false;
    }
}