package ru.rebey.eventmod.network;

import com.google.gson.Gson;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.data.PlayerClass;
import ru.rebey.eventmod.data.PlayerDataHandler;
import ru.rebey.eventmod.data.PlayerDataManager;
import ru.rebey.eventmod.team.PlayerTeam;

import java.util.HashSet;
import java.util.Set;

/**
 * Обработчик выбора класса на сервере.
 * Принимает выбор класса от клиента и сохраняет его.
 */
public class ClassSelectionHandler {
    private static final String LOG_PREFIX = "[ClassSelectionHandler] ";
    private static final Gson GSON = new Gson();

    /**
     * Регистрирует обработчик выбора класса.
     */
    public static void register() {
        EventMod.LOGGER.info("{}Регистрация обработчика выбора класса", LOG_PREFIX);

        ServerPlayNetworking.registerGlobalReceiver(SelectClassPayload.ID, (payload, context) -> {
            try {
                ServerPlayerEntity player = context.player();
                String classId = payload.classId();
                String playerName = player.getName().getString();

                EventMod.LOGGER.info("{}Игрок {} выбрал класс: {}",
                        LOG_PREFIX, playerName, classId);

                // Преобразуем ID в класс игрока
                PlayerClass pc = PlayerClass.fromId(classId);
                if (pc == null) {
                    EventMod.LOGGER.warn("{}Некорректный ID класса от игрока {}: {}",
                            LOG_PREFIX, playerName, classId);

                    player.sendMessage(
                            net.minecraft.text.Text.literal("❌ Неверный класс!")
                                    .formatted(net.minecraft.util.Formatting.RED),
                            true
                    );
                    return;
                }

                EventMod.LOGGER.debug("{}Класс игрока {} преобразован: {} -> {}",
                        LOG_PREFIX, playerName, classId, pc);

                // Сохраняем класс
                PlayerDataHandler.setPlayerClass(player, pc);
                EventMod.LOGGER.info("{}Класс игрока {} сохранен: {}",
                        LOG_PREFIX, playerName, pc);

                // Получаем текущую команду игрока
                PlayerTeam team = PlayerDataHandler.getPlayerTeam(player);
                String teamName = (team != null) ? team.name() : "NONE";

                EventMod.LOGGER.debug("{}Команда игрока {}: {}", LOG_PREFIX, playerName, teamName);

                // Получаем активные эффекты
                Set<String> activeEffects = PlayerDataHandler.getAllActiveEffects(player);
                String effectsJson = GSON.toJson(activeEffects);

                EventMod.LOGGER.debug("{}Активные эффекты игрока {}: {} (JSON: {})",
                        LOG_PREFIX, playerName, activeEffects.size(), effectsJson);

                // Отправляем данные для HUD класса (над хотбаром)
                SyncPlayerEventPlayerDataPayload payloadData = new SyncPlayerEventPlayerDataPayload(
                        pc.name(), teamName, effectsJson
                );

                ServerPlayNetworking.send(player, payloadData);
                EventMod.LOGGER.debug("{}Данные игрока {} отправлены на клиент",
                        LOG_PREFIX, playerName);

                // Отправляем подтверждение игроку
                player.sendMessage(
                        net.minecraft.text.Text.literal("✅ Класс выбран: " + pc.getDisplayName())
                                .formatted(net.minecraft.util.Formatting.GREEN),
                        true
                );

                EventMod.LOGGER.info("{}Обработка выбора класса для игрока {} завершена успешно",
                        LOG_PREFIX, playerName);

            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при обработке выбора класса: {}",
                        LOG_PREFIX, e.getMessage(), e);
            }
        });

        EventMod.LOGGER.info("{}Обработчик выбора класса зарегистрирован", LOG_PREFIX);
    }

    /**
     * Получает статистику по выбору классов.
     * @param server сервер Minecraft
     * @return строка со статистикой
     */
    public static String getClassStats(net.minecraft.server.MinecraftServer server) {
        try {
            int totalPlayers = server.getPlayerManager().getPlayerList().size();
            int playersWithClass = 0;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (PlayerDataHandler.getPlayerClass(player) != null) {
                    playersWithClass++;
                }
            }

            return String.format("Class Stats: Всего игроков=%d, С классом=%d, Без класса=%d",
                    totalPlayers, playersWithClass, totalPlayers - playersWithClass);

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при получении статистики классов: {}",
                    LOG_PREFIX, e.getMessage(), e);
            return "Class Stats: Ошибка при получении статистики";
        }
    }

    /**
     * Получает распределение по классам.
     * @param server сервер Minecraft
     * @return строка с распределением
     */
    public static String getClassDistribution(net.minecraft.server.MinecraftServer server) {
        try {
            java.util.Map<PlayerClass, Integer> classCounts = new java.util.HashMap<>();

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PlayerClass pc = PlayerDataHandler.getPlayerClass(player);
                if (pc != null) {
                    classCounts.put(pc, classCounts.getOrDefault(pc, 0) + 1);
                }
            }

            StringBuilder distribution = new StringBuilder("Class Distribution: ");
            for (PlayerClass pc : PlayerClass.values()) {
                int count = classCounts.getOrDefault(pc, 0);
                distribution.append(String.format("%s=%d ", pc.getDisplayName(), count));
            }

            return distribution.toString().trim();

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при получении распределения классов: {}",
                    LOG_PREFIX, e.getMessage(), e);
            return "Class Distribution: Ошибка при получении распределения";
        }
    }
}