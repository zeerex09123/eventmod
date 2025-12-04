package ru.rebey.eventmod.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import ru.rebey.eventmod.EventMod;

import java.util.ArrayList;
import java.util.List;

/**
 * Пакет для синхронизации списка всех игроков с клиентом.
 * Содержит информацию обо всех игроках на сервере.
 */
public record SyncAllPlayersPayload(List<PlayerInfo> players) implements CustomPayload {
    private static final String LOG_PREFIX = "[SyncAllPlayersPayload] ";

    public static final Id<SyncAllPlayersPayload> ID = new Id<>(Identifier.of(EventMod.MOD_ID, "sync_all_players"));

    /**
     * Кодек для сериализации/десериализации пакета.
     */
    public static final PacketCodec<RegistryByteBuf, SyncAllPlayersPayload> CODEC = new PacketCodec<>() {
        @Override
        public SyncAllPlayersPayload decode(RegistryByteBuf buf) {
            try {
                int size = buf.readVarInt();
                EventMod.LOGGER.trace("{}Декодирование пакета, количество игроков: {}", LOG_PREFIX, size);

                List<PlayerInfo> players = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    PlayerInfo info = PlayerInfo.CODEC.decode(buf);
                    players.add(info);
                    EventMod.LOGGER.trace("{}  Декодирован игрок {}: {}", LOG_PREFIX, i, info);
                }

                return new SyncAllPlayersPayload(players);

            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при декодировании пакета: {}", LOG_PREFIX, e.getMessage(), e);
                return new SyncAllPlayersPayload(new ArrayList<>());
            }
        }

        @Override
        public void encode(RegistryByteBuf buf, SyncAllPlayersPayload payload) {
            try {
                EventMod.LOGGER.trace("{}Кодирование пакета, количество игроков: {}",
                        LOG_PREFIX, payload.players.size());

                buf.writeVarInt(payload.players.size());
                for (PlayerInfo info : payload.players) {
                    PlayerInfo.CODEC.encode(buf, info);
                    EventMod.LOGGER.trace("{}  Закодирован игрок: {}", LOG_PREFIX, info);
                }

            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при кодировании пакета: {}", LOG_PREFIX, e.getMessage(), e);
            }
        }
    };

    /**
     * Информация об игроке для синхронизации.
     * @param name имя игрока
     * @param className название класса
     * @param teamName название команды
     */
    public record PlayerInfo(String name, String className, String teamName) {
        /**
         * Кодек для сериализации/десериализации информации об игроке.
         */
        public static final PacketCodec<RegistryByteBuf, PlayerInfo> CODEC = new PacketCodec<>() {
            @Override
            public PlayerInfo decode(RegistryByteBuf buf) {
                try {
                    String name = buf.readString();
                    String className = buf.readString();
                    String teamName = buf.readString();

                    EventMod.LOGGER.trace("{}Декодирование PlayerInfo: name={}, class={}, team={}",
                            LOG_PREFIX, name, className, teamName);

                    return new PlayerInfo(name, className, teamName);

                } catch (Exception e) {
                    EventMod.LOGGER.error("{}Ошибка при декодировании PlayerInfo: {}", LOG_PREFIX, e.getMessage(), e);
                    return new PlayerInfo("", "", "");
                }
            }

            @Override
            public void encode(RegistryByteBuf buf, PlayerInfo info) {
                try {
                    EventMod.LOGGER.trace("{}Кодирование PlayerInfo: name={}, class={}, team={}",
                            LOG_PREFIX, info.name, info.className, info.teamName);

                    buf.writeString(info.name);
                    buf.writeString(info.className);
                    buf.writeString(info.teamName);

                } catch (Exception e) {
                    EventMod.LOGGER.error("{}Ошибка при кодировании PlayerInfo: {}", LOG_PREFIX, e.getMessage(), e);
                }
            }
        };

        @Override
        public String toString() {
            return String.format("PlayerInfo{name='%s', className='%s', teamName='%s'}",
                    name, className, teamName);
        }

        /**
         * Проверяет, имеет ли игрок класс.
         * @return true если класс установлен
         */
        public boolean hasClass() {
            return className != null && !className.isEmpty();
        }

        /**
         * Проверяет, находится ли игрок в команде.
         * @return true если команда установлена
         */
        public boolean hasTeam() {
            return teamName != null && !teamName.isEmpty() && !"NONE".equals(teamName);
        }

        /**
         * Получает отображаемое имя класса.
         * @return читаемое имя класса или пустая строка
         */
        public String getClassDisplayName() {
            if (className == null || className.isEmpty()) {
                return "";
            }

            switch (className) {
                case "tank": return "🛡️ Танк";
                case "assassin": return "🗡️ Ассасин";
                case "engineer": return "⚙️ Инженер";
                case "mage": return "🔮 Маг";
                case "scout": return "👁️ Разведчик";
                default: return className;
            }
        }
    }

    /**
     * Регистрирует пакет в системе.
     */
    public static void register() {
        EventMod.LOGGER.info("{}Регистрация пакета синхронизации списка игроков", LOG_PREFIX);
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
        EventMod.LOGGER.debug("{}Пакет зарегистрирован с ID: {}", LOG_PREFIX, ID.id());
    }

    @Override
    public Id<SyncAllPlayersPayload> getId() {
        return ID;
    }

    /**
     * Получает количество игроков в пакете.
     * @return количество игроков
     */
    public int getPlayerCount() {
        return players.size();
    }

    /**
     * Проверяет, содержит ли пакет информацию об указанном игроке.
     * @param playerName имя игрока
     * @return true если игрок найден
     */
    public boolean containsPlayer(String playerName) {
        boolean contains = players.stream().anyMatch(info -> info.name.equals(playerName));
        EventMod.LOGGER.trace("{}Проверка наличия игрока '{}' в пакете: {}",
                LOG_PREFIX, playerName, contains);
        return contains;
    }

    /**
     * Получает информацию об игроке по имени.
     * @param playerName имя игрока
     * @return информация об игроке или null если не найден
     */
    public PlayerInfo getPlayerInfo(String playerName) {
        for (PlayerInfo info : players) {
            if (info.name.equals(playerName)) {
                EventMod.LOGGER.trace("{}Найден игрок '{}' в пакете", LOG_PREFIX, playerName);
                return info;
            }
        }

        EventMod.LOGGER.trace("{}Игрок '{}' не найден в пакете", LOG_PREFIX, playerName);
        return null;
    }

    /**
     * Получает статистику по классам игроков.
     * @return строку со статистикой
     */
    public String getClassStatistics() {
        java.util.Map<String, Integer> classCounts = new java.util.HashMap<>();

        for (PlayerInfo info : players) {
            if (info.hasClass()) {
                classCounts.put(info.className, classCounts.getOrDefault(info.className, 0) + 1);
            }
        }

        StringBuilder stats = new StringBuilder("Class Statistics: ");
        for (java.util.Map.Entry<String, Integer> entry : classCounts.entrySet()) {
            stats.append(String.format("%s=%d ", entry.getKey(), entry.getValue()));
        }

        EventMod.LOGGER.trace("{}Статистика классов: {}", LOG_PREFIX, stats.toString());
        return stats.toString().trim();
    }

    /**
     * Получает статистику по командам игроков.
     * @return строку со статистикой
     */
    public String getTeamStatistics() {
        java.util.Map<String, Integer> teamCounts = new java.util.HashMap<>();

        for (PlayerInfo info : players) {
            String team = info.hasTeam() ? info.teamName : "NONE";
            teamCounts.put(team, teamCounts.getOrDefault(team, 0) + 1);
        }

        StringBuilder stats = new StringBuilder("Team Statistics: ");
        for (java.util.Map.Entry<String, Integer> entry : teamCounts.entrySet()) {
            stats.append(String.format("%s=%d ", entry.getKey(), entry.getValue()));
        }

        EventMod.LOGGER.trace("{}Статистика команд: {}", LOG_PREFIX, stats.toString());
        return stats.toString().trim();
    }
}