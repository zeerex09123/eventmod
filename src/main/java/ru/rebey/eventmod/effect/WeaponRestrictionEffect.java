// src/main/java/ru/rebey/eventmod/effect/WeaponRestrictionEffect.java
package ru.rebey.eventmod.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ru.rebey.eventmod.EventMod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WeaponRestrictionEffect {
    private static final Set<UUID> RESTRICTED_PLAYERS = new HashSet<>();
    private static final int CHECK_INTERVAL = 20; // каждые 20 тиков = 1 секунда

    static {
        // Проверяем инвентарь каждую секунду
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % CHECK_INTERVAL != 0) return;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (RESTRICTED_PLAYERS.contains(player.getUuid())) {
                    checkAndDropWeapon(player);
                }
            }
        });
    }

    private static void checkAndDropWeapon(ServerPlayerEntity player) {
        for (net.minecraft.util.Hand hand : net.minecraft.util.Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isEmpty() && !isAllowedWeapon(stack)) {
                player.dropItem(stack, true, true);
                player.setStackInHand(hand, ItemStack.EMPTY);
                player.sendMessage(
                        Text.literal("❌ Запрещённое оружие выброшено!").formatted(Formatting.RED),
                        true
                );
                EventMod.LOGGER.debug("Dropped restricted weapon for {}", player.getName().getString());
            }
        }
    }

    private static boolean isAllowedWeapon(ItemStack stack) {
        // Если это НЕ оружие — разрешено
        if (!isWeapon(stack)) {
            return true;
        }

        // Если это оружие — разрешены ТОЛЬКО кирки
        return stack.isOf(Items.WOODEN_PICKAXE) || stack.isOf(Items.STONE_PICKAXE) ||
                stack.isOf(Items.IRON_PICKAXE) || stack.isOf(Items.GOLDEN_PICKAXE) ||
                stack.isOf(Items.DIAMOND_PICKAXE) || stack.isOf(Items.NETHERITE_PICKAXE);
    }

    private static boolean isWeapon(ItemStack stack) {
        // Определяем, является ли предмет оружием
        return stack.isOf(Items.WOODEN_SWORD) || stack.isOf(Items.STONE_SWORD) ||
                stack.isOf(Items.IRON_SWORD) || stack.isOf(Items.GOLDEN_SWORD) ||
                stack.isOf(Items.DIAMOND_SWORD) || stack.isOf(Items.NETHERITE_SWORD) ||
                stack.isOf(Items.WOODEN_AXE) || stack.isOf(Items.STONE_AXE) ||
                stack.isOf(Items.IRON_AXE) || stack.isOf(Items.GOLDEN_AXE) ||
                stack.isOf(Items.DIAMOND_AXE) || stack.isOf(Items.NETHERITE_AXE) ||
                stack.isOf(Items.BOW) || stack.isOf(Items.CROSSBOW) ||
                stack.isOf(Items.TRIDENT) || stack.isOf(Items.FISHING_ROD); // можно убрать fishing rod
    }

    public static void apply(ServerPlayerEntity player) {
        RESTRICTED_PLAYERS.add(player.getUuid());

        // Сразу очистить инвентарь от запрещённого оружия
        cleanupInventory(player);

        player.sendMessage(
                Text.literal("⚠️ Оружие ограничено: только кирки разрешены!").formatted(Formatting.YELLOW),
                true
        );
        EventMod.LOGGER.info("Applied weapon restriction (drop-only) to {}", player.getName().getString());
    }

    private static void cleanupInventory(ServerPlayerEntity player) {
        var inv = player.getInventory();

        // Проверяем все слоты (включая горячую панель и основной инвентарь)
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && !isAllowedWeapon(stack)) {
                player.dropItem(stack, true, true);
                inv.setStack(i, ItemStack.EMPTY);
            }
        }

        // Проверяем off-hand
        ItemStack offhand = inv.getStack(40); // слот off-hand
        if (!offhand.isEmpty() && !isAllowedWeapon(offhand)) {
            player.dropItem(offhand, true, true);
            inv.setStack(40, ItemStack.EMPTY);
        }
    }

    public static void remove(ServerPlayerEntity player) {
        RESTRICTED_PLAYERS.remove(player.getUuid());
    }
}