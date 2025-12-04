package ru.rebey.eventmod.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import ru.rebey.eventmod.EventMod;

/**
 * Пакет для открытия экрана выбора класса на клиенте.
 * Не содержит данных, только сигнал для открытия экрана.
 */
public record OpenClassSelectionPayload() implements CustomPayload {
    private static final String LOG_PREFIX = "[OpenClassSelectionPayload] ";

    public static final Id<OpenClassSelectionPayload> ID = new Id<>(Identifier.of(EventMod.MOD_ID, "open_class_selection"));

    private static final OpenClassSelectionPayload INSTANCE = new OpenClassSelectionPayload();

    public static final PacketCodec<RegistryByteBuf, OpenClassSelectionPayload> CODEC = PacketCodec.unit(INSTANCE);

    /**
     * Регистрирует пакет в системе.
     */
    public static void register() {
        EventMod.LOGGER.info("{}Регистрация пакета открытия выбора класса", LOG_PREFIX);
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
        EventMod.LOGGER.debug("{}Пакет зарегистрирован с ID: {}", LOG_PREFIX, ID.id());
    }

    @Override
    public Id<OpenClassSelectionPayload> getId() {
        return ID;
    }

    /**
     * Получает экземпляр пакета.
     * @return единственный экземпляр пакета
     */
    public static OpenClassSelectionPayload getInstance() {
        EventMod.LOGGER.trace("{}Получение экземпляра пакета", LOG_PREFIX);
        return INSTANCE;
    }

    /**
     * Проверяет, является ли пакет валидным.
     * @return всегда true для этого пакета
     */
    public boolean isValid() {
        EventMod.LOGGER.trace("{}Проверка валидности пакета", LOG_PREFIX);
        return true;
    }

    /**
     * Логирует информацию о пакете.
     */
    public void logPacketInfo() {
        EventMod.LOGGER.debug("{}Информация о пакете:", LOG_PREFIX);
        EventMod.LOGGER.debug("{}  ID: {}", LOG_PREFIX, ID.id());
        EventMod.LOGGER.debug("{}  Тип: S2C (Server to Client)", LOG_PREFIX);
        EventMod.LOGGER.debug("{}  Назначение: открытие экрана выбора класса", LOG_PREFIX);
    }
}