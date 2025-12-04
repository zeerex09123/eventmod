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

import java.util.*;

/**
 * Команда для принудительной отправки выбора карточек игрокам.
 * Требует уровень разрешения 2.
 */
public class CardsCommand {
    private static final String LOG_PREFIX = "[CardsCommand] ";
    private static final Random RANDOM = new Random();

    /**
     * Регистрирует команду /eventmod cards.
     */
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("eventmod")
                            .requires(src -> src.hasPermissionLevel(2))
                            .then(CommandManager.literal("cards")
                                    .executes(CardsCommand::execute)
                            )
            );
            EventMod.LOGGER.info("{}Команда /eventmod cards зарегистрирована", LOG_PREFIX);
        });
    }

    /**
     * Выполняет команду отправки карточек.
     * @param context контекст выполнения команды
     * @return 1 при успехе, 0 при ошибке
     */
    private static int execute(CommandContext<ServerCommandSource> context) {
        EventMod.LOGGER.info("{}Выполнение команды отправки карточек", LOG_PREFIX);

        var server = context.getSource().getServer();
        var players = server.getPlayerManager().getPlayerList();

        if (players.isEmpty()) {
            EventMod.LOGGER.warn("{}Нет онлайн игроков", LOG_PREFIX);
            context.getSource().sendError(Text.literal("Нет игроков"));
            return 0;
        }

        int sentCount = 0;
        int skippedCount = 0;

        for (ServerPlayerEntity player : players) {
            try {
                PlayerClass pc = PlayerDataHandler.getPlayerClass(player);
                if (pc == null) {
                    EventMod.LOGGER.debug("{}Игрок {} не имеет класса. Пропуск.", LOG_PREFIX, player.getName().getString());
                    skippedCount++;
                    continue;
                }

                CardRegistry.CardOption[] allCards = CardRegistry.getCardsFor(pc);
                if (allCards.length < 2) {
                    EventMod.LOGGER.warn("{}Недостаточно карточек для класса {} игрока {}",
                            LOG_PREFIX, pc, player.getName().getString());
                    skippedCount++;
                    continue;
                }

                // Выбираем 2 случайные карточки
                List<CardRegistry.CardOption> list = new ArrayList<>(List.of(allCards));
                Collections.shuffle(list, RANDOM);
                CardRegistry.CardOption card1 = list.get(0);
                CardRegistry.CardOption card2 = list.get(1);

                // Формируем описание карточек
                Text fullText1 = createFullCardText(card1);
                Text fullText2 = createFullCardText(card2);

                // Отправляем игроку
                ServerPlayNetworking.send(player, OpenCardSelectionPayload.of(
                        fullText1, fullText2, card1.id(), card2.id()
                ));

                EventMod.LOGGER.debug("{}Отправлены карточки игроку {}: {} и {}",
                        LOG_PREFIX, player.getName().getString(), card1.id(), card2.id());
                sentCount++;

            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при отправке карточек игроку {}: {}",
                        LOG_PREFIX, player.getName().getString(), e.getMessage(), e);
                skippedCount++;
            }
        }

        final int finalSentCount = sentCount;
        context.getSource().sendFeedback(
                () -> Text.literal("GUI карточек отправлен " + finalSentCount + " игрокам"),
                true
        );

        EventMod.LOGGER.info("{}Отправка завершена. Успешно: {}, Пропущено: {}",
                LOG_PREFIX, sentCount, skippedCount);
        return 1;
    }

    /**
     * Создает полное описание карточки.
     * @param card карточка для описания
     * @return форматированный текст
     */
    private static Text createFullCardText(CardRegistry.CardOption card) {
        return card.name()
                .copy()
                .append("\n")
                .append(card.buffDescription())
                .append("\n")
                .append(card.debuffDescription());
    }
}