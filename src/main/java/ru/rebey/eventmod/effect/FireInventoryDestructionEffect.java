// src/main/java/ru/rebey/eventmod/effect/FireInventoryDestructionEffect.java
package ru.rebey.eventmod.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.rebey.eventmod.EventMod;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class FireInventoryDestructionEffect {
    private static final Set<UUID> AFFECTED_PLAYERS = new HashSet<>();
    private static final Random RANDOM = new Random();
    private static final int CHECK_INTERVAL = 20; // каждую секунду

    static {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % CHECK_INTERVAL != 0) return;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (AFFECTED_PLAYERS.contains(player.getUuid())) {
                    if (player.isInLava() || player.isOnFire()) {
                        destroyRandomItem(player);
                    }
                }
            }
        });
    }

    private static void destroyRandomItem(ServerPlayerEntity player) {
        var inv = player.getInventory();
        // Выбираем случайный слот из основного инвентаря (0–35)
        int slot = RANDOM.nextInt(36);
        ItemStack stack = inv.getStack(slot);

        if (!stack.isEmpty()) {
            // Уничтожаем предмет
            inv.setStack(slot, ItemStack.EMPTY);
            player.sendMessage(
                    Text.literal("🔥 Предмет уничтожен огнём!").formatted(Formatting.RED),
                    true
            );
            EventMod.LOGGER.info("Destroyed random item from slot {} for player {}", slot, player.getName().getString());
        }
    }

    public static void apply(ServerPlayerEntity player) {
        AFFECTED_PLAYERS.add(player.getUuid());
        EventMod.LOGGER.info("Applied fire inventory destruction effect to {}", player.getName().getString());
    }

    public static void remove(ServerPlayerEntity player) {
        AFFECTED_PLAYERS.remove(player.getUuid());
    }
}