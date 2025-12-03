// src/main/java/ru/rebey/eventmod/effect/EmergencyRegenEffect.java
package ru.rebey.eventmod.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.EventMod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EmergencyRegenEffect {
    // Следим, получал ли игрок реген уже (чтобы не спамить)
    private static final Map<UUID, Boolean> HAS_TRIGGERED = new HashMap<>();
    // После регена — Hunger
    private static final Map<UUID, Long> HUNGER_SCHEDULED = new HashMap<>();

    static {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID uuid = player.getUuid();
                float health = player.getHealth();
                float maxHealth = player.getMaxHealth();
                float percent = health / maxHealth;

                // Если HP < 30% и ещё не сработало
                if (percent < 0.3f && !HAS_TRIGGERED.getOrDefault(uuid, false)) {
                    // Применяем регенерацию
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 2000, 1, false, true));
                    HAS_TRIGGERED.put(uuid, true);
                    EventMod.LOGGER.info("Emergency regen triggered for {}", player.getName().getString());

                    // Запланировать Hunger через 5 секунд (100 тиков) — когда реген закончится
                    HUNGER_SCHEDULED.put(uuid, server.getTicks() + 100L); // L = long
                }

                // Проверяем, не пора ли Hunger
                Long hungerTick = HUNGER_SCHEDULED.get(uuid);
                if (hungerTick != null && server.getTicks() >= hungerTick) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 12000, 0, false, true)); // 10 минут = 12000 тиков
                    HUNGER_SCHEDULED.remove(uuid);
                    EventMod.LOGGER.info("Applied post-regen Hunger to {}", player.getName().getString());
                }
            }
        });
    }

    public static void apply(ServerPlayerEntity player) {
        // Сброс триггера при повторном применении (если карта получена снова)
        HAS_TRIGGERED.remove(player.getUuid());
        HUNGER_SCHEDULED.remove(player.getUuid());
        EventMod.LOGGER.debug("Emergency regen armed for {}", player.getName().getString());
    }
}