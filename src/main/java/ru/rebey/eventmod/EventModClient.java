// src/main/java/ru/rebey/eventmod/EventModClient.java
package ru.rebey.eventmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import ru.rebey.eventmod.data.ClientPlayerData;
import ru.rebey.eventmod.data.ClientPlayerList;
import ru.rebey.eventmod.gui.ClassSelectionScreen;
import ru.rebey.eventmod.gui.CardSelectionScreen;
import ru.rebey.eventmod.hud.ClassHudRenderer;
import ru.rebey.eventmod.hud.PlayerListHudRenderer;
import ru.rebey.eventmod.network.*;

public class EventModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(OpenClassSelectionPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                MinecraftClient.getInstance().setScreen(new ClassSelectionScreen());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(OpenCardSelectionPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                MinecraftClient.getInstance().setScreen(new CardSelectionScreen(
                        payload.card1Text(), payload.card2Text(), payload.card1Id(), payload.card2Id()
                ));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(
                SyncPlayerEventPlayerDataPayload.ID,
                (payload, context) -> {
                    context.client().execute(() -> {
                        ClientPlayerData.setClass(payload.className());
                        ClientPlayerData.setTeamColor(payload.teamColor());
                    });
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(SyncAllPlayersPayload.ID, (payload, context) -> {
            EventMod.LOGGER.info("Received player list: {} players", payload.players().size());
            context.client().execute(() -> {
                ClientPlayerList.setPlayers(payload.players());
            });
        });

        ClassHudRenderer.register();
        PlayerListHudRenderer.register();
    }
}