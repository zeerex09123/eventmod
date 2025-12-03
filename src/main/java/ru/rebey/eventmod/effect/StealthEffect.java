// src/main/java/ru/rebey/eventmod/effect/StealthEffect.java
package ru.rebey.eventmod.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import ru.rebey.eventmod.EventMod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StealthEffect {
    private static final Map<UUID, Long> SNEAK_START_TICK = new HashMap<>();
    private static final Map<UUID, Boolean> IS_STEALTHED = new HashMap<>();
    private static final long STEALTH_DURATION_TICKS = 60; // 3 секунды = 60 тиков

    static {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (isAffected(player)) {
                    updateStealth(player, server.getTicks());
                }
            }
        });
    }

    private static boolean isAffected(ServerPlayerEntity player) {
        return AFFECTED_PLAYERS.contains(player.getUuid());
    }

    private static final java.util.Set<UUID> AFFECTED_PLAYERS = new java.util.HashSet<>();

    public static void apply(ServerPlayerEntity player) {
        AFFECTED_PLAYERS.add(player.getUuid());
        EventMod.LOGGER.info("Stealth effect armed for {}", player.getName().getString());
    }

    private static void updateStealth(ServerPlayerEntity player, long currentTick) {
        UUID uuid = player.getUuid();
        boolean isSneaking = player.isSneaking();

        if (isSneaking) {
            // Игрок сидит на шифте
            Long startTick = SNEAK_START_TICK.get(uuid);
            if (startTick == null) {
                SNEAK_START_TICK.put(uuid, currentTick);
                startTick = currentTick;
            }

            // Через 3 секунды — активируем невидимость
            if (currentTick - startTick >= STEALTH_DURATION_TICKS && !IS_STEALTHED.getOrDefault(uuid, false)) {
                activateStealth(player);
                IS_STEALTHED.put(uuid, true);
            }

            // Убедимся, что эффекты активны
            if (IS_STEALTHED.getOrDefault(uuid, false)) {
                // Slowness I = -15% скорости (близко к "в 2 раза медленее" — можно усилить до Slowness II)
                if (!player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * 10, 0, false, false));
                }
                if (!player.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 20 * 10, 0, false, false));
                }
            }
        } else {
            // Игрок отпустил шифт
            if (IS_STEALTHED.getOrDefault(uuid, false)) {
                deactivateStealth(player);
                IS_STEALTHED.put(uuid, false);
            }
            SNEAK_START_TICK.remove(uuid);
        }
    }

    private static void activateStealth(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 999999, 0, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 999999, 0, false, false)); // Slowness I
        EventMod.LOGGER.debug("Stealth activated for {}", player.getName().getString());
    }

    private static void deactivateStealth(ServerPlayerEntity player) {
        player.removeStatusEffect(StatusEffects.INVISIBILITY);
        player.removeStatusEffect(StatusEffects.SLOWNESS);
        // Добавляем Speed II на 5 секунд
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 100, 1, false, true)); // 100 тиков = 5 сек, уровень 1 = Speed II
        EventMod.LOGGER.debug("Stealth deactivated, speed boost applied to {}", player.getName().getString());
    }
}