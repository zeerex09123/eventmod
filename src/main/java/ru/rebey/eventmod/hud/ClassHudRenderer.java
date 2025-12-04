package ru.rebey.eventmod.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.data.ClientPlayerData;
import ru.rebey.eventmod.data.PlayerClass;

/**
 * Рендерер HUD для отображения класса и команды игрока.
 * Отображается над хотбаром по центру.
 */
public class ClassHudRenderer {
    private static final String LOG_PREFIX = "[ClassHudRenderer] ";

    /**
     * Регистрирует рендерер HUD.
     */
    public static void register() {
        EventMod.LOGGER.info("{}Регистрация рендерера HUD класса", LOG_PREFIX);
        HudRenderCallback.EVENT.register(ClassHudRenderer::render);
    }

    /**
     * Отрисовывает HUD класса.
     * @param context контекст отрисовки
     * @param counter счетчик рендера
     */
    private static void render(DrawContext context, RenderTickCounter counter) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();

            // Проверяем условия отрисовки
            if (client.player == null || client.options.hudHidden) {
                EventMod.LOGGER.trace("{}Пропуск отрисовки: игрок null или HUD скрыт", LOG_PREFIX);
                return;
            }

            // Получаем класс игрока
            PlayerClass pc = ClientPlayerData.getPlayerClass();
            if (pc == null) {
                EventMod.LOGGER.trace("{}Пропуск отрисовки: класс игрока не установлен", LOG_PREFIX);
                return;
            }

            // Формируем текст класса
            String classText = getClassDisplayText(pc);
            EventMod.LOGGER.trace("{}Отрисовка класса игрока: {}", LOG_PREFIX, classText);

            // Получаем цвет команды
            String teamColor = ClientPlayerData.getTeamColor();
            Formatting fmt = getTeamFormatting(teamColor);

            // Создаем текст с форматированием
            Text text = Text.literal(classText).formatted(fmt);
            TextRenderer tr = client.textRenderer;

            // Вычисляем позицию (по центру, над хотбаром)
            int x = (context.getScaledWindowWidth() - tr.getWidth(text)) / 2;
            int y = context.getScaledWindowHeight() - 55; // над хотбаром

            // Отрисовываем текст
            context.drawText(tr, text, x, y, 0xFFFFFF, true);

            EventMod.LOGGER.trace("{}Класс отрисован в позиции ({}, {}): {}",
                    LOG_PREFIX, x, y, classText);

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при отрисовке HUD класса: {}",
                    LOG_PREFIX, e.getMessage(), e);
        }
    }

    /**
     * Получает отображаемый текст для класса.
     * @param pc класс игрока
     * @return текст для отображения
     */
    private static String getClassDisplayText(PlayerClass pc) {
        switch (pc) {
            case TANK:
                return "🛡️ Танк";
            case ASSASSIN:
                return "🗡️ Ассасин";
            case ENGINEER:
                return "⚙️ Инженер";
            case MAGE:
                return "🔮 Маг";
            case SCOUT:
                return "👁️ Разведчик";
            default:
                EventMod.LOGGER.warn("{}Неизвестный класс: {}", LOG_PREFIX, pc);
                return "❓";
        }
    }

    /**
     * Получает форматирование для команды.
     * @param teamColor цвет команды
     * @return форматирование
     */
    private static Formatting getTeamFormatting(String teamColor) {
        switch (teamColor) {
            case "red":
                EventMod.LOGGER.trace("{}Команда игрока: красная", LOG_PREFIX);
                return Formatting.RED;
            case "blue":
                EventMod.LOGGER.trace("{}Команда игрока: синяя", LOG_PREFIX);
                return Formatting.BLUE;
            default:
                EventMod.LOGGER.trace("{}Команда игрока: нет или неизвестна ({})", LOG_PREFIX, teamColor);
                return Formatting.WHITE;
        }
    }

    /**
     * Получает цвет команды в формате RGB.
     * @param teamColor цвет команды
     * @return цвет в формате 0xRRGGBB
     */
    private static int getTeamColorRGB(String teamColor) {
        switch (teamColor) {
            case "red":
                return 0xFF5555;
            case "blue":
                return 0x5555FF;
            default:
                return 0xFFFFFF;
        }
    }

    /**
     * Проверяет, должен ли отображаться HUD.
     * @return true если HUD должен отображаться
     */
    public static boolean shouldRender() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null &&
                !client.options.hudHidden &&
                ClientPlayerData.getPlayerClass() != null;
    }

    /**
     * Получает текущий класс игрока для отображения.
     * @return текст класса или null
     */
    public static String getCurrentClassText() {
        PlayerClass pc = ClientPlayerData.getPlayerClass();
        return pc != null ? getClassDisplayText(pc) : null;
    }

    /**
     * Получает текущий цвет команды игрока.
     * @return цвет команды
     */
    public static String getCurrentTeamColor() {
        return ClientPlayerData.getTeamColor();
    }
}