package ru.rebey.eventmod.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import ru.rebey.eventmod.EventMod;

/**
 * Пакет для отправки выбора класса с клиента на сервер.
 * Содержит ID выбранного класса.
 */
public record SelectClassPayload(String classId) implements CustomPayload {
    private static final String LOG_PREFIX = "[SelectClassPayload] ";

    public static final Id<SelectClassPayload> ID = new Id<>(Identifier.of(EventMod.MOD_ID, "select_class"));

    public static final PacketCodec<RegistryByteBuf, SelectClassPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SelectClassPayload::classId,
            SelectClassPayload::new
    );

    /**
     * Регистрирует пакет в системе.
     */
    public static void register() {
        EventMod.LOGGER.info("{}Регистрация пакета выбора класса", LOG_PREFIX);
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        EventMod.LOGGER.debug("{}Пакет зарегистрирован с ID: {}", LOG_PREFIX, ID.id());
    }

    @Override
    public Id<SelectClassPayload> getId() {
        return ID;
    }

    /**
     * Получает ID выбранного класса.
     * @return ID класса
     */
    public String getClassId() {
        EventMod.LOGGER.trace("{}Получение ID класса из пакета: {}", LOG_PREFIX, classId);
        return classId;
    }

    /**
     * Проверяет валидность пакета.
     * @return true если пакет валиден
     */
    public boolean isValid() {
        boolean valid = classId != null && !classId.isEmpty();

        if (!valid) {
            EventMod.LOGGER.warn("{}Пакет невалиден: classId={}", LOG_PREFIX, classId);
        } else {
            EventMod.LOGGER.trace("{}Пакет валиден: {}", LOG_PREFIX, classId);
        }

        return valid;
    }

    /**
     * Создает новый пакет с указанным ID класса.
     * @param classId ID класса
     * @return объект пакета
     */
    public static SelectClassPayload create(String classId) {
        EventMod.LOGGER.debug("{}Создание пакета выбора класса с ID: {}", LOG_PREFIX, classId);

        if (classId == null || classId.isEmpty()) {
            EventMod.LOGGER.error("{}Попытка создания пакета с пустым ID класса", LOG_PREFIX);
            throw new IllegalArgumentException("ID класса не может быть пустым");
        }

        return new SelectClassPayload(classId);
    }

    /**
     * Проверяет, является ли ID класса допустимым.
     * @return true если ID класса допустим
     */
    public boolean isClassIdValid() {
        String[] validClassIds = {"tank", "assassin", "engineer", "mage", "scout"};
        boolean isValid = false;

        for (String validId : validClassIds) {
            if (validId.equals(classId)) {
                isValid = true;
                break;
            }
        }

        EventMod.LOGGER.trace("{}Проверка допустимости ID класса {}: {}",
                LOG_PREFIX, classId, isValid);

        return isValid;
    }

    /**
     * Логирует информацию о пакете.
     */
    public void logPacketInfo() {
        EventMod.LOGGER.debug("{}Информация о пакете выбора класса:", LOG_PREFIX);
        EventMod.LOGGER.debug("{}  ID класса: {}", LOG_PREFIX, classId);
        EventMod.LOGGER.debug("{}  Валидность ID: {}", LOG_PREFIX, isClassIdValid());
        EventMod.LOGGER.debug("{}  Тип: C2S (Client to Server)", LOG_PREFIX);
        EventMod.LOGGER.debug("{}  Назначение: отправка выбора класса на сервер", LOG_PREFIX);
    }
}