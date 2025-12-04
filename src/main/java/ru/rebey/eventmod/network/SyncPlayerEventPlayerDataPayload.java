package ru.rebey.eventmod.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import ru.rebey.eventmod.EventMod;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * Пакет для синхронизации данных игрока с клиентом.
 * Содержит информацию о классе, команде и эффектах игрока.
 */
public record SyncPlayerEventPlayerDataPayload(String className, String teamColor, String effectsJson) implements CustomPayload {
    private static final String LOG_PREFIX = "[SyncPlayerEventPlayerDataPayload] ";

    public static final Id<SyncPlayerEventPlayerDataPayload> ID = new Id<>(Identifier.of(EventMod.MOD_ID, "sync_player_data"));

    private static final Gson GSON = new Gson();
    private static final TypeToken<Set<String>> SET_TYPE_TOKEN = new TypeToken<Set<String>>() {};

    /**
     * Кодек для сериализации/десериализации пакета.
     */
    public static final PacketCodec<RegistryByteBuf, SyncPlayerEventPlayerDataPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SyncPlayerEventPlayerDataPayload::className,
            PacketCodecs.STRING, SyncPlayerEventPlayerDataPayload::teamColor,
            PacketCodecs.STRING, SyncPlayerEventPlayerDataPayload::effectsJson,
            SyncPlayerEventPlayerDataPayload::new
    );

    /**
     * Регистрирует пакет в системе.
     */
    public static void register() {
        EventMod.LOGGER.info("{}Регистрация пакета синхронизации данных игрока", LOG_PREFIX);
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
        EventMod.LOGGER.debug("{}Пакет зарегистрирован с ID: {}", LOG_PREFIX, ID.id());
    }

    /**
     * Получает набор эффектов из JSON строки.
     * @return множество ID эффектов
     */
    public Set<String> getEffects() {
        try {
            if (effectsJson == null || effectsJson.isEmpty()) {
                EventMod.LOGGER.trace("{}JSON эффектов пуст, возвращаем пустой набор", LOG_PREFIX);
                return new HashSet<>();
            }

            Set<String> effects = GSON.fromJson(effectsJson, SET_TYPE_TOKEN.getType());
            EventMod.LOGGER.trace("{}Декодировано {} эффектов из JSON", LOG_PREFIX, effects.size());
            return effects;

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при парсинге JSON эффектов: {}, JSON: {}",
                    LOG_PREFIX, e.getMessage(), effectsJson, e);
            return new HashSet<>();
        }
    }

    /**
     * Создает JSON строку из набора эффектов.
     * @param effects множество ID эффектов
     * @return JSON строка
     */
    public static String createEffectsJson(Set<String> effects) {
        try {
            String json = GSON.toJson(effects);
            EventMod.LOGGER.trace("{}Создан JSON для {} эффектов", LOG_PREFIX, effects.size());
            return json;

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при создании JSON эффектов: {}", LOG_PREFIX, e.getMessage(), e);
            return "[]";
        }
    }

    @Override
    public Id<SyncPlayerEventPlayerDataPayload> getId() {
        return ID;
    }

    /**
     * Получает название класса игрока.
     * @return название класса
     */
    public String getClassName() {
        EventMod.LOGGER.trace("{}Получение названия класса из пакета: {}", LOG_PREFIX, className);
        return className;
    }

    /**
     * Получает цвет команды игрока.
     * @return цвет команды
     */
    public String getTeamColor() {
        EventMod.LOGGER.trace("{}Получение цвета команды из пакета: {}", LOG_PREFIX, teamColor);
        return teamColor;
    }

    /**
     * Проверяет, имеет ли игрок установленный класс.
     * @return true если класс установлен
     */
    public boolean hasClass() {
        boolean hasClass = className != null && !className.isEmpty();
        EventMod.LOGGER.trace("{}Проверка наличия класса: {}", LOG_PREFIX, hasClass);
        return hasClass;
    }

    /**
     * Проверяет, находится ли игрок в команде.
     * @return true если команда установлена
     */
    public boolean hasTeam() {
        boolean hasTeam = teamColor != null && !teamColor.isEmpty() && !"none".equals(teamColor);
        EventMod.LOGGER.trace("{}Проверка наличия команды: {}", LOG_PREFIX, hasTeam);
        return hasTeam;
    }

    /**
     * Получает количество эффектов игрока.
     * @return количество эффектов
     */
    public int getEffectCount() {
        int count = getEffects().size();
        EventMod.LOGGER.trace("{}Количество эффектов: {}", LOG_PREFIX, count);
        return count;
    }

    /**
     * Проверяет валидность пакета.
     * @return true если пакет валиден
     */
    public boolean isValid() {
        boolean valid = className != null && teamColor != null && effectsJson != null;

        if (!valid) {
            EventMod.LOGGER.warn("{}Пакет невалиден: className={}, teamColor={}, effectsJson={}",
                    LOG_PREFIX, className, teamColor, effectsJson);
        } else {
            EventMod.LOGGER.trace("{}Пакет валиден", LOG_PREFIX);
        }

        return valid;
    }

    /**
     * Создает новый пакет с указанными данными.
     * @param className название класса
     * @param teamColor цвет команды
     * @param effects набор эффектов
     * @return объект пакета
     */
    public static SyncPlayerEventPlayerDataPayload create(String className, String teamColor, Set<String> effects) {
        EventMod.LOGGER.debug("{}Создание пакета синхронизации данных игрока", LOG_PREFIX);
        EventMod.LOGGER.debug("{}  Класс: {}", LOG_PREFIX, className);
        EventMod.LOGGER.debug("{}  Команда: {}", LOG_PREFIX, teamColor);
        EventMod.LOGGER.debug("{}  Эффектов: {}", LOG_PREFIX, effects.size());

        String effectsJson = createEffectsJson(effects);
        return new SyncPlayerEventPlayerDataPayload(className, teamColor, effectsJson);
    }

    /**
     * Логирует информацию о пакете.
     */
    public void logPacketInfo() {
        EventMod.LOGGER.debug("{}Информация о пакете синхронизации данных игрока:", LOG_PREFIX);
        EventMod.LOGGER.debug("{}  Класс: {}", LOG_PREFIX, className);
        EventMod.LOGGER.debug("{}  Команда: {}", LOG_PREFIX, teamColor);
        EventMod.LOGGER.debug("{}  Эффектов: {}", LOG_PREFIX, getEffectCount());
        EventMod.LOGGER.debug("{}  Тип: S2C (Server to Client)", LOG_PREFIX);
        EventMod.LOGGER.debug("{}  Назначение: синхронизация данных игрока с клиентом", LOG_PREFIX);
    }
}