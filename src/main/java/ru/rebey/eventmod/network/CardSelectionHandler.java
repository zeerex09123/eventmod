// src/main/java/ru/rebey/eventmod/network/CardSelectionHandler.java
package ru.rebey.eventmod.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.data.PlayerClass;
import ru.rebey.eventmod.data.PlayerDataHandler;
import ru.rebey.eventmod.effect.*;

public class CardSelectionHandler {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(SelectCardPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            String cardId = payload.cardId();

            PlayerClass pc = PlayerDataHandler.getPlayerClass(player);
            if (pc == null) {
                EventMod.LOGGER.warn("Player {} has no class", player.getName().getString());
                return;
            }

            EventMod.LOGGER.info("Player {} selected card {}", player.getName().getString(), cardId);

            switch (pc) {
                case TANK -> handleTank(player, cardId);
                case ASSASSIN -> handleAssassin(player, cardId);
                default -> EventMod.LOGGER.warn("Unsupported class: {}", pc);
            }
        });
    }

    private static void handleTank(ServerPlayerEntity p, String id) {
        switch (id) {
            case "tank_card_1" -> {
                new ExtraHealthEffect(8.0).apply(p);
                new SlownessEffect(1).apply(p);
            }
            case "tank_card_2" -> {
                new SlowFallingEffect().apply(p);
                WeaponRestrictionEffect.apply(p);
            }
            case "tank_card_3" -> EmergencyRegenEffect.apply(p);
            default -> EventMod.LOGGER.warn("Unknown tank card: {}", id);
        }
    }

    private static void handleAssassin(ServerPlayerEntity p, String id) {
        switch (id) {
            case "assassin_card_1" -> new AssassinCard1Effect().apply(p);
            case "assassin_card_2" -> {
                StealthEffect.apply(p);
                LeatherArmorOnlyEffect.apply(p);
            }
            case "assassin_card_3" -> {
                new IncreasedSpeedEffect().apply(p);
                FireInventoryDestructionEffect.apply(p);
            }
            default -> EventMod.LOGGER.warn("Unknown assassin card: {}", id);
        }
    }
}