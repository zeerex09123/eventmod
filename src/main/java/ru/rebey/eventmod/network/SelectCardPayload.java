package ru.rebey.eventmod.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import ru.rebey.eventmod.EventMod;

/**
 * Пакет для отправки выбора карточки с клиента на сервер.
 * Содержит ID выбранной карточки.
 */
public record SelectCardPayload(String cardId) implements CustomPayload {
    private static final String LOG_PREFIX = "[SelectCardPayload] ";

    public static final Id<SelectCardPayload> ID = new Id<>(Identifier.of(EventMod.MOD_ID, "select_card"));

    public static final PacketCodec<RegistryByteBuf, SelectCardPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SelectCardPayload::cardId,
            SelectCardPayload::new
    );

    /**
     * Регистрирует пакет в системе.
     */
    public static void register() {
        EventMod.LOGGER.info("{}Регистрация пакета выбора карточки", LOG_PREFIX);
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        EventMod.LOGGER.debug("{}Пакет зарегистрирован с ID: {}", LOG_PREFIX, ID.id());
    }

    @Override
    public Id<SelectCardPayload> getId() {
        return ID;
    }

    /**
     * Получает ID выбранной карточки.
     * @return ID карточки
     */
    public String getCardId() {
        EventMod.LOGGER.trace("{}Получение ID карточки из пакета: {}", LOG_PREFIX, cardId);
        return cardId;
    }

    /**
     * Проверяет валидность пакета.
     * @return true если пакет валиден
     */
    public boolean isValid() {
        boolean valid = cardId != null && !cardId.isEmpty();

        if (!valid) {
            EventMod.LOGGER.warn("{}Пакет невалиден: cardId={}", LOG_PREFIX, cardId);
        } else {
            EventMod.LOGGER.trace("{}Пакет валиден: {}", LOG_PREFIX, cardId);
        }

        return valid;
    }

    /**
     * Создает новый пакет с указанным ID карточки.
     * @param cardId ID карточки
     * @return объект пакета
     */
    public static SelectCardPayload create(String cardId) {
        EventMod.LOGGER.debug("{}Создание пакета выбора карточки с ID: {}", LOG_PREFIX, cardId);

        if (cardId == null || cardId.isEmpty()) {
            EventMod.LOGGER.error("{}Попытка создания пакета с пустым ID карточки", LOG_PREFIX);
            throw new IllegalArgumentException("ID карточки не может быть пустым");
        }

        return new SelectCardPayload(cardId);
    }

    /**
     * Логирует информацию о пакете.
     */
    public void logPacketInfo() {
        EventMod.LOGGER.debug("{}Информация о пакете выбора карточки:", LOG_PREFIX);
        EventMod.LOGGER.debug("{}  ID карточки: {}", LOG_PREFIX, cardId);
        EventMod.LOGGER.debug("{}  Тип: C2S (Client to Server)", LOG_PREFIX);
        EventMod.LOGGER.debug("{}  Назначение: отправка выбора карточки на сервер", LOG_PREFIX);
    }
}