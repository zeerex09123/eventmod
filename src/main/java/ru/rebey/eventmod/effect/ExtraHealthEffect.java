// src/main/java/ru/rebey/eventmod/effect/ExtraHealthEffect.java
package ru.rebey.eventmod.effect;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import ru.rebey.eventmod.EventMod;

public class ExtraHealthEffect implements CardEffect {
    // Используем Identifier вместо UUID
    private static final Identifier HEALTH_MODIFIER_ID = Identifier.of("eventmod", "extra_health");
    private final double extraHealth; // в единицах HP (1 сердце = 2 HP)

    public ExtraHealthEffect(double extraHealth) {
        this.extraHealth = extraHealth;
    }

    @Override
    public void apply(ServerPlayerEntity player) {
        EventMod.LOGGER.info("[ExtraHealthEffect] Применение эффекта для {}: +{} HP",
                player.getName().getString(), extraHealth);
        EventMod.LOGGER.info("[ExtraHealthEffect] Текущее здоровье до: {}/{}",
                player.getHealth(), player.getMaxHealth());

        var attr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (attr != null) {
            // Удаляем старый модификатор по Identifier
            EventMod.LOGGER.info("[ExtraHealthEffect] Удаляем старый модификатор");
            attr.removeModifier(HEALTH_MODIFIER_ID);

            // Добавляем новый
            var modifier = new EntityAttributeModifier(
                    HEALTH_MODIFIER_ID,
                    extraHealth,
                    EntityAttributeModifier.Operation.ADD_VALUE
            );
            attr.addPersistentModifier(modifier);
            EventMod.LOGGER.info("[ExtraHealthEffect] Добавлен новый модификатор: +{} HP", extraHealth);

            player.setHealth(player.getHealth()); // обновить текущее здоровье
            EventMod.LOGGER.info("[ExtraHealthEffect] Здоровье после: {}/{}",
                    player.getHealth(), player.getMaxHealth());
        } else {
            EventMod.LOGGER.error("[ExtraHealthEffect] Не удалось получить атрибут здоровья у игрока {}",
                    player.getName().getString());
        }
    }
}