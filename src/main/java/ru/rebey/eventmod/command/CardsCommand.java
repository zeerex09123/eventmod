// src/main/java/ru/rebey/eventmod/command/CardsCommand.java
package ru.rebey.eventmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.card.CardRegistry;
import ru.rebey.eventmod.data.PlayerClass;
import ru.rebey.eventmod.data.PlayerDataHandler;
import ru.rebey.eventmod.network.OpenCardSelectionPayload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CardsCommand {
    private static final Random RANDOM = new Random();

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("eventmod")
                            .requires(src -> src.hasPermissionLevel(2))
                            .then(CommandManager.literal("cards")
                                    .executes(CardsCommand::execute)
                            )
            );
        });
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        var server = context.getSource().getServer();
        var players = server.getPlayerManager().getPlayerList();
        if (players.isEmpty()) {
            context.getSource().sendError(Text.literal("Нет игроков"));
            return 0;
        }

        int sentCount = 0;
        for (ServerPlayerEntity player : players) { // ✅ Исправлено: ServerPlayerEntity
            PlayerClass pc = PlayerDataHandler.getPlayerClass(player);
            if (pc == null) continue;

            CardRegistry.CardOption[] allCards = CardRegistry.getCardsFor(pc);
            if (allCards.length < 2) continue;

            List<CardRegistry.CardOption> list = new ArrayList<>(List.of(allCards));
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

            ServerPlayNetworking.send(player, OpenCardSelectionPayload.of(
                    fullText1, fullText2, card1.id(), card2.id()
            ));
            sentCount++;
        }

        int finalSentCount = sentCount;
        context.getSource().sendFeedback(
                () -> Text.literal("GUI карточек отправлен " + finalSentCount + " игрокам"),
                true
        );
        return 1;
    }
}