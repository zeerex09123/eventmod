// src/main/java/ru/rebey/eventmod/effect/AssassinCard1Effect.java
package ru.rebey.eventmod.effect;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import ru.rebey.eventmod.EventMod;

public class AssassinCard1Effect implements CardEffect {
    private static final Identifier HEALTH_MODIFIER_ID = Identifier.of("eventmod", "assassin_health_reduction");

    @Override
    public void apply(ServerPlayerEntity player) {
        // Бафф: Strength I (+30% урона)
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, -1, 0, false, false));

        // Дебафф: −20% здоровья → −4 HP (от базовых 20)
        var healthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            // Удаляем старый модификатор (на случай повторного применения)
            healthAttr.removeModifier(HEALTH_MODIFIER_ID);

            // Добавляем новый: −4.0 HP
            healthAttr.addPersistentModifier(new EntityAttributeModifier(
                    HEALTH_MODIFIER_ID,
                    -4.0,
                    EntityAttributeModifier.Operation.ADD_VALUE
            ));
            player.setHealth(player.getHealth()); // обновить текущее здоровье
        }

        EventMod.LOGGER.info("Applied Assassin Card 1: Strength + Reduced Health to {}", player.getName().getString());
    }
}