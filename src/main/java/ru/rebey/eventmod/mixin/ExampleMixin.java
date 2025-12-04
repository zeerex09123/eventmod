package ru.rebey.eventmod.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.rebey.eventmod.EventMod;

/**
 * Пример Mixin для демонстрации работы.
 * Внедряет код в начало метода loadWorld() сервера Minecraft.
 */
@Mixin(MinecraftServer.class)
public class ExampleMixin {
    private static final String LOG_PREFIX = "[ExampleMixin] ";

    /**
     * Внедряет код в начало метода MinecraftServer.loadWorld().
     * @param info callback информация
     */
    @Inject(at = @At("HEAD"), method = "loadWorld")
    private void init(CallbackInfo info) {
        try {
            // This code is injected into the start of MinecraftServer.loadWorld()V
            EventMod.LOGGER.info("{}Сервер загружает мир (внедрено через Mixin)", LOG_PREFIX);

            // Можно добавить дополнительную логику инициализации здесь
            EventMod.LOGGER.debug("{}Mixin успешно внедрен в метод loadWorld", LOG_PREFIX);

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка в Mixin при загрузке мира: {}",
                    LOG_PREFIX, e.getMessage(), e);
        }
    }

    /**
     * Вспомогательный метод для логирования состояния сервера.
     */
    private void logServerState(MinecraftServer server) {
        if (server != null) {
            EventMod.LOGGER.debug("{}Состояние сервера:", LOG_PREFIX);
            EventMod.LOGGER.debug("{}  Игроков онлайн: {}",
                    LOG_PREFIX, server.getPlayerManager().getPlayerList().size());
            EventMod.LOGGER.debug("{}  Макс игроков: {}",
                    LOG_PREFIX, server.getMaxPlayerCount());
            EventMod.LOGGER.debug("{}  Запущен: {}",
                    LOG_PREFIX, server.isRunning());
        }
    }
}