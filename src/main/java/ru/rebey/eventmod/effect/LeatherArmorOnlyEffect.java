// src/main/java/ru/rebey/eventmod/effect/LeatherArmorOnlyEffect.java
package ru.rebey.eventmod.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.rebey.eventmod.EventMod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LeatherArmorOnlyEffect {
    private static final Set<UUID> AFFECTED_PLAYERS = new HashSet<>();
    private static final int CHECK_INTERVAL = 20; // каждую секунду

    static {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % CHECK_INTERVAL != 0) return;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (AFFECTED_PLAYERS.contains(player.getUuid())) {
                    enforceLeatherArmor(player);
                }
            }
        });
    }

    private static void enforceLeatherArmor(ServerPlayerEntity player) {
        boolean dropped = false;
        var inv = player.getInventory();

        // Слоты брони: 36 (шлем), 37 (нагрудник), 38 (штаны), 39 (ботинки)
        for (int slot = 36; slot <= 39; slot++) {
            ItemStack stack = inv.getStack(slot);
            if (!stack.isEmpty() && !isLeatherArmor(stack)) {
                player.dropItem(stack, true, true);
                inv.setStack(slot, ItemStack.EMPTY);
                dropped = true;
            }
        }

        if (dropped) {
            player.sendMessage(
                    Text.literal("❌ Только кожаная броня разрешена!").formatted(Formatting.RED),
                    true
            );
        }
    }

    private static boolean isLeatherArmor(ItemStack stack) {
        return stack.isOf(Items.LEATHER_HELMET) ||
                stack.isOf(Items.LEATHER_CHESTPLATE) ||
                stack.isOf(Items.LEATHER_LEGGINGS) ||
                stack.isOf(Items.LEATHER_BOOTS);
    }

    public static void apply(ServerPlayerEntity player) {
        AFFECTED_PLAYERS.add(player.getUuid());
        enforceLeatherArmor(player); // сразу очистить
        EventMod.LOGGER.info("Applied 'Leather armor only' effect to {}", player.getName().getString());
    }
}