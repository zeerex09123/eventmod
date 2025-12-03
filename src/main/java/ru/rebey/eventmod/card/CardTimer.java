// src/main/java/ru/rebey/eventmod/card/CardTimer.java
package ru.rebey.eventmod.card;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.data.PlayerClass;
import ru.rebey.eventmod.data.PlayerDataHandler;
import ru.rebey.eventmod.network.OpenCardSelectionPayload;

import java.util.*;

public class CardTimer {
    private static int getTicksBetweenCards() {
        return 5 * 20 * 60; // 5 минут
    }
    private static long lastTriggerTick = -1;
    private static final Random RANDOM = new Random();

    public static void register(MinecraftServer server) {
        lastTriggerTick = server.getTicks();
        ServerTickEvents.END_SERVER_TICK.register(s -> {
            if (s.getTicks() - lastTriggerTick >= getTicksBetweenCards()) {
                triggerCardSelection(s);
                lastTriggerTick = s.getTicks();
            }
        });
    }

    private static void triggerCardSelection(MinecraftServer server) {
        EventMod.LOGGER.info("Card selection timer triggered. Generating cards for players...");
        int count = 0;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerClass pc = PlayerDataHandler.getPlayerClass(player);
            if (pc == null) continue;

            CardRegistry.CardOption[] allCards = CardRegistry.getCardsFor(pc);
            if (allCards.length < 2) {
                EventMod.LOGGER.warn("Not enough cards for class {} (found {})", pc, allCards.length);
                continue;
            }

            List<CardRegistry.CardOption> list = new ArrayList<>(Arrays.asList(allCards));
            Collections.shuffle(list, RANDOM);
            CardRegistry.CardOption card1 = list.get(0);
            CardRegistry.CardOption card2 = list.get(1);

            Text fullText1 = card1.name()
                    .copy()
                    .append("\n")
                    .append(card1.buffDescription())
                    .append("\n")
                    .append(card1.debuffDescription());

            Text fullText2 = card2.name()
                    .copy()
                    .append("\n")
                    .append(card2.buffDescription())
                    .append("\n")
                    .append(card2.debuffDescription());

            ServerPlayNetworking.send(player, OpenCardSelectionPayload.of(fullText1, fullText2, card1.id(), card2.id()));
            EventMod.LOGGER.debug("Sent card selection to player: {}", player.getName().getString());
            count++;
        }
        EventMod.LOGGER.info("Card selection GUI sent to {} players.", count);
    }
}