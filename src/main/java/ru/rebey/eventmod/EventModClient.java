package ru.rebey.eventmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import ru.rebey.eventmod.data.ClientPlayerData;
import ru.rebey.eventmod.data.ClientPlayerList;
import ru.rebey.eventmod.gui.ClassSelectionScreen;
import ru.rebey.eventmod.gui.CardSelectionScreen;
import ru.rebey.eventmod.hud.ClassHudRenderer;
import ru.rebey.eventmod.hud.PlayerListHudRenderer;
import ru.rebey.eventmod.network.*;

import java.util.Set;

/**
 * Клиентский класс мода EventMod.
 * Регистрирует все клиентские системы и обработчики.
 */
public class EventModClient implements ClientModInitializer {
    private static final String LOG_PREFIX = "[EventModClient] ";

    /**
     * Инициализирует мод на клиенте.
     * Вызывается Fabric при запуске клиента.
     */
    @Override
    public void onInitializeClient() {
        EventMod.LOGGER.info("{}Инициализация клиентской части EventMod", LOG_PREFIX);

        try {
            // Регистрируем S2C пакеты (сервер→клиент) на клиенте
            EventMod.LOGGER.info("{}Регистрация сетевых пакетов...", LOG_PREFIX);
            registerNetworkPackages();

            // Регистрируем обработчики пакетов
            EventMod.LOGGER.info("{}Регистрация обработчиков пакетов...", LOG_PREFIX);
            registerPacketHandlers();

            // Регистрируем HUD рендереры
            EventMod.LOGGER.info("{}Регистрация HUD рендереров...", LOG_PREFIX);
            registerHudRenderers();

            EventMod.LOGGER.info("{}Клиентская часть EventMod успешно инициализирована.", LOG_PREFIX);

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Критическая ошибка при инициализации клиентской части: {}",
                    LOG_PREFIX, e.getMessage(), e);
            throw new RuntimeException("Ошибка инициализации клиентской части EventMod", e);
        }
    }

    /**
     * Регистрирует сетевые пакеты на клиенте.
     */
    private void registerNetworkPackages() {
        try {
            OpenClassSelectionPayload.register();
            EventMod.LOGGER.debug("{}Пакет OpenClassSelectionPayload зарегистрирован", LOG_PREFIX);

            OpenCardSelectionPayload.register();
            EventMod.LOGGER.debug("{}Пакет OpenCardSelectionPayload зарегистрирован", LOG_PREFIX);

            SyncPlayerEventPlayerDataPayload.register();
            EventMod.LOGGER.debug("{}Пакет SyncPlayerEventPlayerDataPayload зарегистрирован", LOG_PREFIX);

            SyncAllPlayersPayload.register();
            EventMod.LOGGER.debug("{}Пакет SyncAllPlayersPayload зарегистрирован", LOG_PREFIX);

            EventMod.LOGGER.info("{}Все сетевые пакеты успешно зарегистрированы на клиенте", LOG_PREFIX);

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при регистрации сетевых пакетов на клиенте: {}",
                    LOG_PREFIX, e.getMessage(), e);
        }
    }

    /**
     * Регистрирует обработчики сетевых пакетов.
     */
    private void registerPacketHandlers() {
        try {
            // Обработчик открытия экрана выбора класса
            ClientPlayNetworking.registerGlobalReceiver(OpenClassSelectionPayload.ID, (payload, context) -> {
                try {
                    EventMod.LOGGER.info("{}Получен пакет открытия экрана выбора класса", LOG_PREFIX);

                    context.client().execute(() -> {
                        try {
                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.player != null) {
                                EventMod.LOGGER.debug("{}Открытие экрана выбора класса для игрока {}",
                                        LOG_PREFIX, client.player.getName().getString());

                                client.setScreen(new ClassSelectionScreen());
                                EventMod.LOGGER.debug("{}Экран выбора класса успешно открыт", LOG_PREFIX);
                            } else {
                                EventMod.LOGGER.warn("{}Игрок не найден, невозможно открыть экран выбора класса",
                                        LOG_PREFIX);
                            }
                        } catch (Exception e) {
                            EventMod.LOGGER.error("{}Ошибка при открытии экрана выбора класса: {}",
                                    LOG_PREFIX, e.getMessage(), e);
                        }
                    });

                } catch (Exception e) {
                    EventMod.LOGGER.error("{}Ошибка при обработке пакета открытия класса: {}",
                            LOG_PREFIX, e.getMessage(), e);
                }
            });

            // Обработчик открытия экрана выбора карточек
            ClientPlayNetworking.registerGlobalReceiver(OpenCardSelectionPayload.ID, (payload, context) -> {
                try {
                    EventMod.LOGGER.info("{}Получен пакет открытия экрана выбора карточек. Карточки: {} и {}",
                            LOG_PREFIX, payload.card1Id(), payload.card2Id());

                    context.client().execute(() -> {
                        try {
                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.player != null) {
                                EventMod.LOGGER.debug("{}Открытие экрана выбора карточек для игрока {}",
                                        LOG_PREFIX, client.player.getName().getString());

                                client.setScreen(new CardSelectionScreen(
                                        payload.card1Text(), payload.card2Text(),
                                        payload.card1Id(), payload.card2Id()
                                ));
                                EventMod.LOGGER.debug("{}Экран выбора карточек успешно открыт", LOG_PREFIX);
                            } else {
                                EventMod.LOGGER.warn("{}Игрок не найден, невозможно открыть экран выбора карточек",
                                        LOG_PREFIX);
                            }
                        } catch (Exception e) {
                            EventMod.LOGGER.error("{}Ошибка при открытии экрана выбора карточек: {}",
                                    LOG_PREFIX, e.getMessage(), e);
                        }
                    });

                } catch (Exception e) {
                    EventMod.LOGGER.error("{}Ошибка при обработке пакета выбора карточек: {}",
                            LOG_PREFIX, e.getMessage(), e);
                }
            });

            // Обработчик синхронизации данных игрока
            ClientPlayNetworking.registerGlobalReceiver(
                    SyncPlayerEventPlayerDataPayload.ID,
                    (payload, context) -> {
                        try {
                            EventMod.LOGGER.info("{}Получен пакет синхронизации данных игрока. Класс: {}, Команда: {}",
                                    LOG_PREFIX, payload.className(), payload.teamColor());

                            context.client().execute(() -> {
                                try {
                                    // Обновляем данные игрока на клиенте
                                    ClientPlayerData.setClass(payload.className());
                                    ClientPlayerData.setTeamColor(payload.teamColor());

                                    // Обновляем эффекты на клиенте
                                    ClientPlayerData.clearEffects();
                                    Set<String> effects = payload.getEffects();

                                    for (String effect : effects) {
                                        ClientPlayerData.addEffect(effect);
                                        EventMod.LOGGER.trace("{}  Добавлен эффект на клиенте: {}", LOG_PREFIX, effect);
                                    }

                                    EventMod.LOGGER.debug("{}Данные игрока обновлены на клиенте. Эффектов: {}",
                                            LOG_PREFIX, effects.size());

                                } catch (Exception e) {
                                    EventMod.LOGGER.error("{}Ошибка при обновлении данных игрока на клиенте: {}",
                                            LOG_PREFIX, e.getMessage(), e);
                                }
                            });

                        } catch (Exception e) {
                            EventMod.LOGGER.error("{}Ошибка при обработке пакета синхронизации данных: {}",
                                    LOG_PREFIX, e.getMessage(), e);
                        }
                    }
            );

            // Обработчик синхронизации списка игроков
            ClientPlayNetworking.registerGlobalReceiver(SyncAllPlayersPayload.ID, (payload, context) -> {
                try {
                    EventMod.LOGGER.info("{}Получен пакет списка игроков: {} игроков",
                            LOG_PREFIX, payload.players().size());

                    context.client().execute(() -> {
                        try {
                            ClientPlayerList.setPlayers(payload.players());
                            EventMod.LOGGER.debug("{}Список игроков обновлен на клиенте. Игроков: {}",
                                    LOG_PREFIX, payload.players().size());

                            // Логируем статистику для отладки
                            if (EventMod.LOGGER.isDebugEnabled()) {
                                EventMod.LOGGER.debug("{}Статистика полученного списка:", LOG_PREFIX);
                                EventMod.LOGGER.debug("{}  Всего игроков: {}", LOG_PREFIX, payload.getPlayerCount());
                                EventMod.LOGGER.debug("{}  Распределение по классам: {}",
                                        LOG_PREFIX, payload.getClassStatistics());
                                EventMod.LOGGER.debug("{}  Распределение по командам: {}",
                                        LOG_PREFIX, payload.getTeamStatistics());
                            }

                        } catch (Exception e) {
                            EventMod.LOGGER.error("{}Ошибка при обновлении списка игроков на клиенте: {}",
                                    LOG_PREFIX, e.getMessage(), e);
                        }
                    });

                } catch (Exception e) {
                    EventMod.LOGGER.error("{}Ошибка при обработке пакета списка игроков: {}",
                            LOG_PREFIX, e.getMessage(), e);
                }
            });

            EventMod.LOGGER.info("{}Все обработчики пакетов успешно зарегистрированы", LOG_PREFIX);

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при регистрации обработчиков пакетов: {}",
                    LOG_PREFIX, e.getMessage(), e);
        }
    }

    /**
     * Регистрирует HUD рендереры.
     */
    private void registerHudRenderers() {
        try {
            ClassHudRenderer.register();
            EventMod.LOGGER.debug("{}HUD рендерер класса зарегистрирован", LOG_PREFIX);

            PlayerListHudRenderer.register();
            EventMod.LOGGER.debug("{}HUD рендерер списка игроков зарегистрирован", LOG_PREFIX);

            EventMod.LOGGER.info("{}Все HUD рендереры успешно зарегистрированы", LOG_PREFIX);

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при регистрации HUD рендереров: {}", LOG_PREFIX, e.getMessage(), e);
        }
    }

    /**
     * Получает статистику клиентской части мода.
     * @return строка со статистикой
     */
    public static String getClientStats() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            String playerName = client.player != null ? client.player.getName().getString() : "Нет игрока";

            return String.format("EventMod Client Stats: Игрок=%s, HUD активен=%s",
                    playerName, ClassHudRenderer.shouldRender());

        } catch (Exception e) {
            return "EventMod Client Stats: Ошибка при получении статистики";
        }
    }

    /**
     * Проверяет, инициализирована ли клиентская часть мода.
     * @return true если клиентская часть инициализирована
     */
    public static boolean isClientInitialized() {
        return true; // Если мы находимся в этом классе, значит клиентская часть инициализирована
    }

    /**
     * Получает текущего игрока на клиенте.
     * @return текущий игрок или null
     */
    public static net.minecraft.client.network.ClientPlayerEntity getClientPlayer() {
        try {
            return MinecraftClient.getInstance().player;
        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при получении клиентского игрока: {}", LOG_PREFIX, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Получает имя текущего игрока на клиенте.
     * @return имя игрока или "Неизвестно"
     */
    public static String getClientPlayerName() {
        net.minecraft.client.network.ClientPlayerEntity player = getClientPlayer();
        return player != null ? player.getName().getString() : "Неизвестно";
    }
}