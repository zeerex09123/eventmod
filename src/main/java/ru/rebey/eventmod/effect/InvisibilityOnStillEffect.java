// src/main/java/ru/rebey/eventmod/effect/InvisibilityOnStillEffect.java
package ru.rebey.eventmod.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import ru.rebey.eventmod.EventMod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class InvisibilityOnStillEffect {
    private static final Map<UUID, Long> LAST_MOVE_TICK = new HashMap<>();
    private static final Map<UUID, Boolean> HAS_INVISIBILITY = new HashMap<>();
    private static final Set<UUID> AFFECTED_PLAYERS = new HashSet<>();
    private static final long STILL_DURATION_TICKS = 200L; // 10 секунд

    static {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (AFFECTED_PLAYERS.contains(player.getUuid())) {
                    Vec3d motion = player.getVelocity();
                    boolean isStill = motion.x == 0.0 && motion.z == 0.0 && !player.isSneaking();

                    if (isStill) {
                        Long lastMove = LAST_MOVE_TICK.getOrDefault(player.getUuid(), 0L);
                        if (server.getTicks() - lastMove >= STILL_DURATION_TICKS) {
                            if (!HAS_INVISIBILITY.getOrDefault(player.getUuid(), false)) {
                                applyInvisibility(player);
                                HAS_INVISIBILITY.put(player.getUuid(), true);
                            }
                        }
                    } else {
                        // Движение — обновляем время последнего движения
                        LAST_MOVE_TICK.put(player.getUuid(), (long) server.getTicks()); //
                        if (HAS_INVISIBILITY.getOrDefault(player.getUuid(), false)) {
                            removeInvisibility(player);
                            HAS_INVISIBILITY.put(player.getUuid(), false);
                        }
                    }
                }
            }
        });
    }

    public static void apply(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        AFFECTED_PLAYERS.add(uuid);
        LAST_MOVE_TICK.put(uuid, (long) player.getServer().getTicks()); // ✅
        HAS_INVISIBILITY.put(uuid, false);
    }

    private static void applyInvisibility(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 100, 0, false, false));
        EventMod.LOGGER.debug("Invisibility applied to {}", player.getName().getString());
    }

    private static void removeInvisibility(ServerPlayerEntity player) {
        player.removeStatusEffect(StatusEffects.INVISIBILITY);
    }
}