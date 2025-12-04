package ru.rebey.eventmod.network;

import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import ru.rebey.eventmod.EventMod;

/**
 * Пакет для открытия экрана выбора карточек на клиенте.
 * Содержит информацию о двух случайных карточках.
 */
public record OpenCardSelectionPayload(String card1Json, String card2Json, String card1Id, String card2Id) implements CustomPayload {
    private static final String LOG_PREFIX = "[OpenCardSelectionPayload] ";

    public static final Id<OpenCardSelectionPayload> ID = new Id<>(Identifier.of(EventMod.MOD_ID, "open_card_selection"));

    public static final PacketCodec<RegistryByteBuf, OpenCardSelectionPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, OpenCardSelectionPayload::card1Json,
            PacketCodecs.STRING, OpenCardSelectionPayload::card2Json,
            PacketCodecs.STRING, OpenCardSelectionPayload::card1Id,
            PacketCodecs.STRING, OpenCardSelectionPayload::card2Id,
            OpenCardSelectionPayload::new
    );

    /**
     * Преобразует JSON строку в объект Text для первой карточки.
     * @return объект Text или текст с ошибкой
     */
    public Text card1Text() {
        try {
            return TextCodecs.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(card1Json))
                    .result()
                    .orElse(Text.literal("[Ошибка загрузки карточки 1]"));
        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при парсинге JSON карточки 1: {}", LOG_PREFIX, e.getMessage(), e);
            return Text.literal("[Ошибка: " + e.getMessage() + "]");
        }
    }

    /**
     * Преобразует JSON строку в объект Text для второй карточки.
     * @return объект Text или текст с ошибкой
     */
    public Text card2Text() {
        try {
            return TextCodecs.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(card2Json))
                    .result()
                    .orElse(Text.literal("[Ошибка загрузки карточки 2]"));
        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при парсинге JSON карточки 2: {}", LOG_PREFIX, e.getMessage(), e);
            return Text.literal("[Ошибка: " + e.getMessage() + "]");
        }
    }

    /**
     * Создает пакет из объектов Text и ID карточек.
     * @param c1 текст первой карточки
     * @param c2 текст второй карточки
     * @param id1 ID первой карточки
     * @param id2 ID второй карточки
     * @return объект пакета
     */
    public static OpenCardSelectionPayload of(Text c1, Text c2, String id1, String id2) {
        EventMod.LOGGER.debug("{}Создание пакета выбора карточек: {} и {}", LOG_PREFIX, id1, id2);

        try {
            String j1 = TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, c1)
                    .result()
                    .map(Object::toString)
                    .orElse("\"[Ошибка преобразования карточки 1]\"");

            String j2 = TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, c2)
                    .result()
                    .map(Object::toString)
                    .orElse("\"[Ошибка преобразования карточки 2]\"");

            EventMod.LOGGER.trace("{}JSON карточки 1: {}", LOG_PREFIX, j1);
            EventMod.LOGGER.trace("{}JSON карточки 2: {}", LOG_PREFIX, j2);

            return new OpenCardSelectionPayload(j1, j2, id1, id2);

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при создании пакета выбора карточек: {}", LOG_PREFIX, e.getMessage(), e);
            return new OpenCardSelectionPayload(
                    "\"[Ошибка]\"", "\"[Ошибка]\"", "error", "error"
            );
        }
    }

    /**
     * Регистрирует пакет в системе.
     */
    public static void register() {
        EventMod.LOGGER.info("{}Регистрация пакета открытия выбора карточек", LOG_PREFIX);
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
        EventMod.LOGGER.debug("{}Пакет зарегистрирован с ID: {}", LOG_PREFIX, ID.id());
    }

    @Override
    public Id<OpenCardSelectionPayload> getId() {
        return ID;
    }

    /**
     * Получает ID первой карточки.
     * @return ID первой карточки
     */
    public String getCard1Id() {
        return card1Id;
    }

    /**
     * Получает ID второй карточки.
     * @return ID второй карточки
     */
    public String getCard2Id() {
        return card2Id;
    }

    /**
     * Проверяет валидность пакета.
     * @return true если пакет валиден
     */
    public boolean isValid() {
        boolean valid = !card1Id.equals("error") && !card2Id.equals("error");

        if (!valid) {
            EventMod.LOGGER.warn("{}Пакет невалиден: card1Id={}, card2Id={}",
                    LOG_PREFIX, card1Id, card2Id);
        }

        return valid;
    }
}