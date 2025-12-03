// src/main/java/ru/rebey/eventmod/network/ClassSelectionHandler.java
package ru.rebey.eventmod.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.data.PlayerClass;
import ru.rebey.eventmod.data.PlayerDataHandler;
import ru.rebey.eventmod.team.PlayerTeam;

public class ClassSelectionHandler {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(SelectClassPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            String classId = payload.classId();

            PlayerClass pc = PlayerClass.fromId(classId);
            if (pc == null) {
                EventMod.LOGGER.warn("Invalid class ID: {}", classId);
                return;
            }

            // Сохраняем класс
            PlayerDataHandler.setPlayerClass(player, pc);
            EventMod.LOGGER.info("Player {} selected class {}", player.getName().getString(), pc);

            // Получаем текущую команду игрока
            PlayerTeam team = PlayerDataHandler.getPlayerTeam(player);
            String teamName = (team != null) ? team.name() : "NONE";

            // Отправляем данные для HUD класса (над хотбаром)
            ServerPlayNetworking.send(player, new SyncPlayerEventPlayerDataPayload(pc.name(), teamName));

        });
    }
}