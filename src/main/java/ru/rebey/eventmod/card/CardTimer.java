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

/**
 * Таймер для автоматической отправки выбора карточек игрокам.
 * Отправляет выбор карточек каждые 5 минут.
 */
public class CardTimer {
    private static final String LOG_PREFIX = "[CardTimer] ";

    // Интервал между отправками карточек (5 минут в тиках)
    private static int getTicksBetweenCards() {
        return 5 * 20 * 60; // 5 минут
    }

    private static long lastTriggerTick = -1;
    private static final Random RANDOM = new Random();

    /**
     * Регистрирует таймер на сервере.
     * @param server экземпляр сервера Minecraft
     */
    public static void register(MinecraftServer server) {
        lastTriggerTick = server.getTicks();
        EventMod.LOGGER.info("{}Таймер карточек зарегистрирован. Интервал: {} тиков ({} минут)",
                LOG_PREFIX, getTicksBetweenCards(), getTicksBetweenCards() / (20 * 60));

        ServerTickEvents.END_SERVER_TICK.register(s -> {
            if (s.getTicks() - lastTriggerTick >= getTicksBetweenCards()) {
                triggerCardSelection(s);
                lastTriggerTick = s.getTicks();
            }
        });
    }

    /**
     * Запускает процесс выбора карточек для всех игроков онлайн.
     * @param server экземпляр сервера Minecraft
     */
    private static void triggerCardSelection(MinecraftServer server) {
        EventMod.LOGGER.info("{}Таймер сработал. Генерация карточек для игроков...", LOG_PREFIX);

        int totalPlayers = server.getPlayerManager().getPlayerList().size();
        int successfulSends = 0;
        int failedSends = 0;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            try {
                PlayerClass pc = PlayerDataHandler.getPlayerClass(player);
                if (pc == null) {
                    EventMod.LOGGER.warn("{}Игрок {} не имеет класса. Пропуск.", LOG_PREFIX, player.getName().getString());
                    failedSends++;
                    continue;
                }

                CardRegistry.CardOption[] allCards = CardRegistry.getCardsFor(pc);
                if (allCards.length < 2) {
                    EventMod.LOGGER.warn("{}Недостаточно карточек для класса {} (найдено: {})",
                            LOG_PREFIX, pc, allCards.length);
                    failedSends++;
                    continue;
                }

                // Выбираем 2 случайные карточки
                List<CardRegistry.CardOption> cardList = new ArrayList<>(Arrays.asList(allCards));
                Collections.shuffle(cardList, RANDOM);
                CardRegistry.CardOption card1 = cardList.get(0);
                CardRegistry.CardOption card2 = cardList.get(1);

                // Формируем полное описание карточек
                Text fullText1 = createFullCardText(card1);
                Text fullText2 = createFullCardText(card2);

                // Отправляем игроку пакет с выбором карточек
                ServerPlayNetworking.send(player,
                        OpenCardSelectionPayload.of(fullText1, fullText2, card1.id(), card2.id()));

                EventMod.LOGGER.debug("{}Отправлен выбор карточек игроку: {}. Карточки: {}, {}",
                        LOG_PREFIX, player.getName().getString(), card1.id(), card2.id());
                successfulSends++;

            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при отправке карточек игроку {}: {}",
                        LOG_PREFIX, player.getName().getString(), e.getMessage(), e);
                failedSends++;
            }
        }

        EventMod.LOGGER.info("{}Выбор карточек отправлен {} игрокам. Успешно: {}, Неудачно: {}",
                LOG_PREFIX, totalPlayers, successfulSends, failedSends);
    }

    /**
     * Создает полное текстовое описание карточки.
     * @param card карточка для описания
     * @return форматированный текст с названием и эффектами
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