package ru.rebey.eventmod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import ru.rebey.eventmod.card.CardTimer;
import ru.rebey.eventmod.command.*;
import ru.rebey.eventmod.data.PlayerEventHandler;
import ru.rebey.eventmod.network.*;
import ru.rebey.eventmod.playerlist.PlayerListSync;

/**
 * Главный класс мода EventMod - серверная инициализация.
 * Регистрирует все системы мода на сервере.
 */
public class EventMod implements ModInitializer {
    private static final String LOG_PREFIX = "[EventMod] ";

    public static final String MOD_ID = "eventmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Инициализирует мод на сервере.
     * Вызывается Fabric при запуске сервера.
     */
    @Override
    public void onInitialize() {
        LOGGER.info("{}Инициализация EventMod v1.0", LOG_PREFIX);

        try {
            // Регистрируем команды
            LOGGER.info("{}Регистрация команд...", LOG_PREFIX);
            registerCommands();

            // Регистрируем C2S пакеты (клиент→сервер) на сервере
            LOGGER.info("{}Регистрация сетевых пакетов...", LOG_PREFIX);
            registerNetworkPackages();

            // Регистрируем обработчики
            LOGGER.info("{}Регистрация обработчиков...", LOG_PREFIX);
            registerHandlers();

            LOGGER.info("{}EventMod успешно инициализирован.", LOG_PREFIX);

            // Регистрируем события жизненного цикла сервера
            registerServerLifecycleEvents();

        } catch (Exception e) {
            LOGGER.error("{}Критическая ошибка при инициализации мода: {}", LOG_PREFIX, e.getMessage(), e);
            throw new RuntimeException("Ошибка инициализации EventMod", e);
        }
    }

    /**
     * Регистрирует все команды мода.
     */
    private void registerCommands() {
        try {
            ModCommands.register();
            LOGGER.debug("{}Команда /chooseclass зарегистрирована", LOG_PREFIX);

            TeamCommand.register();
            LOGGER.debug("{}Команда /eventmod teams зарегистрирована", LOG_PREFIX);

            CardsCommand.register();
            LOGGER.debug("{}Команда /eventmod cards зарегистрирована", LOG_PREFIX);

            ResetEffectsCommand.register();
            LOGGER.debug("{}Команда /eventmod reseteffects зарегистрирована", LOG_PREFIX);

            ResetMyEffectsCommand.register();
            LOGGER.debug("{}Команда /reseteffects зарегистрирована", LOG_PREFIX);

            MyEffectsCommand.register();
            LOGGER.debug("{}Команда /myeffects зарегистрирована", LOG_PREFIX);

            LOGGER.info("{}Все команды успешно зарегистрированы", LOG_PREFIX);

        } catch (Exception e) {
            LOGGER.error("{}Ошибка при регистрации команд: {}", LOG_PREFIX, e.getMessage(), e);
        }
    }

    /**
     * Регистрирует сетевые пакеты.
     */
    private void registerNetworkPackages() {
        try {
            SelectClassPayload.register();
            LOGGER.debug("{}Пакет SelectClassPayload зарегистрирован", LOG_PREFIX);

            SelectCardPayload.register();
            LOGGER.debug("{}Пакет SelectCardPayload зарегистрирован", LOG_PREFIX);

            LOGGER.info("{}Сетевые пакеты успешно зарегистрированы", LOG_PREFIX);

        } catch (Exception e) {
            LOGGER.error("{}Ошибка при регистрации сетевых пакетов: {}", LOG_PREFIX, e.getMessage(), e);
        }
    }

    /**
     * Регистрирует обработчики событий.
     */
    private void registerHandlers() {
        try {
            ClassSelectionHandler.register();
            LOGGER.debug("{}Обработчик выбора класса зарегистрирован", LOG_PREFIX);

            CardSelectionHandler.register();
            LOGGER.debug("{}Обработчик выбора карточек зарегистрирован", LOG_PREFIX);

            PlayerEventHandler.register();
            LOGGER.debug("{}Обработчик событий игрока зарегистрирован", LOG_PREFIX);

            LOGGER.info("{}Все обработчики успешно зарегистрированы", LOG_PREFIX);

        } catch (Exception e) {
            LOGGER.error("{}Ошибка при регистрации обработчиков: {}", LOG_PREFIX, e.getMessage(), e);
        }
    }

    /**
     * Регистрирует события жизненного цикла сервера.
     */
    private void registerServerLifecycleEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try {
                LOGGER.info("{}Сервер запущен. Инициализация серверных компонентов...", LOG_PREFIX);

                // Регистрируем таймер карточек
                CardTimer.register(server);
                LOGGER.debug("{}Таймер карточек зарегистрирован", LOG_PREFIX);

                // Регистрируем синхронизатор списка игроков
                PlayerListSync.register(server);
                LOGGER.debug("{}Синхронизатор списка игроков зарегистрирован", LOG_PREFIX);

                LOGGER.info("{}Серверные компоненты EventMod зарегистрированы.", LOG_PREFIX);

                // Логируем информацию о сервере
                logServerInfo(server);

            } catch (Exception e) {
                LOGGER.error("{}Ошибка при инициализации серверных компонентов: {}", LOG_PREFIX, e.getMessage(), e);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("{}Сервер останавливается. Завершение работы EventMod...", LOG_PREFIX);
        });
    }

    /**
     * Логирует информацию о сервере.
     * @param server экземпляр сервера Minecraft
     */
    private void logServerInfo(net.minecraft.server.MinecraftServer server) {
        try {
            LOGGER.info("{}Информация о сервере:", LOG_PREFIX);
            LOGGER.info("{}  Имя мира: {}", LOG_PREFIX, server.getSaveProperties().getLevelName());
            LOGGER.info("{}  Макс игроков: {}", LOG_PREFIX, server.getMaxPlayerCount());
            LOGGER.info("{}  Онлайн игроков: {}", LOG_PREFIX, server.getPlayerManager().getPlayerList().size());

        } catch (Exception e) {
            LOGGER.error("{}Ошибка при получении информации о сервере: {}", LOG_PREFIX, e.getMessage(), e);
        }
    }

    /**
     * Получает статистику работы мода.
     * @return строка со статистикой
     */
    public static String getModStats() {
        return String.format("EventMod Stats: Мод успешно инициализирован, Logger=%s",
                LOGGER.getName());
    }

    /**
     * Проверяет, инициализирован ли мод.
     * @return true если мод инициализирован
     */
    public static boolean isInitialized() {
        return LOGGER != null;
    }

    /**
     * Получает версию мода (заглушка, в реальности нужно читать из mods.toml).
     * @return версия мода
     */
    public static String getModVersion() {
        return "1.0.0";
    }

    /**
     * Получает имя мода.
     * @return имя мода
     */
    public static String getModName() {
        return "Event Mod";
    }
}