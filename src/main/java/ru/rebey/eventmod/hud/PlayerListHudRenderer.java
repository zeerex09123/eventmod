package ru.rebey.eventmod.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.data.ClientPlayerList;
import ru.rebey.eventmod.network.SyncAllPlayersPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Рендерер HUD для отображения списка игроков.
 * Отображает всех онлайн игроков с их классами и командами.
 */
public class PlayerListHudRenderer {
    private static final String LOG_PREFIX = "[PlayerListHudRenderer] ";

    /**
     * Регистрирует рендерер HUD списка игроков.
     */
    public static void register() {
        EventMod.LOGGER.info("{}Регистрация рендерера HUD списка игроков", LOG_PREFIX);
        HudRenderCallback.EVENT.register(PlayerListHudRenderer::render);
    }

    /**
     * Отрисовывает HUD списка игроков.
     * @param context контекст отрисовки
     * @param counter счетчик рендера
     */
    private static void render(DrawContext context, RenderTickCounter counter) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();

            // Проверяем условия отрисовки
            if (client == null || client.options.hudHidden) {
                EventMod.LOGGER.trace("{}Пропуск отрисовки: клиент null или HUD скрыт", LOG_PREFIX);
                return;
            }

            // Получаем список игроков
            var players = ClientPlayerList.getPlayers();
            if (players.isEmpty()) {
                EventMod.LOGGER.trace("{}Пропуск отрисовки: список игроков пуст", LOG_PREFIX);
                return;
            }

            EventMod.LOGGER.trace("{}Отрисовка списка из {} игроков", LOG_PREFIX, players.size());

            // Группировка игроков по командам
            List<SyncAllPlayersPayload.PlayerInfo> redTeam = new ArrayList<>();
            List<SyncAllPlayersPayload.PlayerInfo> blueTeam = new ArrayList<>();
            List<SyncAllPlayersPayload.PlayerInfo> noTeam = new ArrayList<>();

            for (var info : players) {
                String team = info.teamName();
                if ("RED".equals(team)) {
                    redTeam.add(info);
                } else if ("BLUE".equals(team)) {
                    blueTeam.add(info);
                } else {
                    noTeam.add(info);
                }
            }

            // Собираем отсортированный список
            List<SyncAllPlayersPayload.PlayerInfo> sortedPlayers = new ArrayList<>();
            sortedPlayers.addAll(redTeam);
            sortedPlayers.addAll(blueTeam);
            sortedPlayers.addAll(noTeam);

            if (sortedPlayers.isEmpty()) {
                EventMod.LOGGER.trace("{}Отсортированный список пуст", LOG_PREFIX);
                return;
            }

            // Получаем рендерер текста
            TextRenderer tr = client.textRenderer;
            int padding = 6;
            int lineHeight = 12;
            int itemHeight = lineHeight + 2;

            // Рассчитываем размеры HUD
            int maxNameWidth = calculateMaxNameWidth(sortedPlayers, tr);
            int headerWidth = tr.getWidth(Text.literal("👥 Игроки").formatted(Formatting.BOLD, Formatting.YELLOW));

            int contentWidth = Math.max(maxNameWidth, headerWidth);
            int boxWidth = contentWidth + padding * 2;
            int boxHeight = padding * 2 + lineHeight + 4 + sortedPlayers.size() * itemHeight;

            // Рассчитываем позицию (правый верхний угол)
            int x = context.getScaledWindowWidth() - boxWidth - 10;
            int y = (context.getScaledWindowHeight() - boxHeight) / 2;

            // Отрисовываем фон и рамку
            renderBackground(context, x, y, boxWidth, boxHeight);

            // Отрисовываем заголовок
            renderHeader(context, tr, x + padding, y + padding);

            // Отрисовываем список игроков
            renderPlayerList(context, tr, sortedPlayers, x + padding, y + padding + lineHeight + 4, itemHeight);

            EventMod.LOGGER.trace("{}Список игроков отрисован. Позиция: ({}, {}), Размер: {}x{}",
                    LOG_PREFIX, x, y, boxWidth, boxHeight);

        } catch (Exception e) {
            EventMod.LOGGER.error("{}Ошибка при отрисовке HUD списка игроков: {}",
                    LOG_PREFIX, e.getMessage(), e);
        }
    }

    /**
     * Рассчитывает максимальную ширину имен игроков.
     * @param players список игроков
     * @param tr рендерер текста
     * @return максимальная ширина
     */
    private static int calculateMaxNameWidth(List<SyncAllPlayersPayload.PlayerInfo> players, TextRenderer tr) {
        int maxWidth = 0;
        for (var info : players) {
            int width = tr.getWidth(info.name()); // только имя, без иконок
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        EventMod.LOGGER.trace("{}Максимальная ширина имени: {}", LOG_PREFIX, maxWidth);
        return maxWidth;
    }

    /**
     * Отрисовывает фон и рамку HUD.
     * @param context контекст отрисовки
     * @param x позиция X
     * @param y позиция Y
     * @param width ширина
     * @param height высота
     */
    private static void renderBackground(DrawContext context, int x, int y, int width, int height) {
        // Фон
        context.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0x90000000);

        // Рамка
        context.drawBorder(x - 2, y - 2, x + width + 2, y + height + 2, 0xFF8000);

        EventMod.LOGGER.trace("{}Отрисован фон: позиция ({}, {}), размер {}x{}",
                LOG_PREFIX, x, y, width, height);
    }

    /**
     * Отрисовывает заголовок HUD.
     * @param context контекст отрисовки
     * @param tr рендерер текста
     * @param x позиция X
     * @param y позиция Y
     */
    private static void renderHeader(DrawContext context, TextRenderer tr, int x, int y) {
        Text headerText = Text.literal("👥 Игроки").formatted(Formatting.BOLD, Formatting.YELLOW);
        context.drawText(tr, headerText, x, y, 0xFFFFFF, false);

        EventMod.LOGGER.trace("{}Отрисован заголовок: '{}' в позиции ({}, {})",
                LOG_PREFIX, headerText.getString(), x, y);
    }

    /**
     * Отрисовывает список игроков.
     * @param context контекст отрисовки
     * @param tr рендерер текста
     * @param players список игроков
     * @param startX начальная позиция X
     * @param startY начальная позиция Y
     * @param itemHeight высота элемента
     */
    private static void renderPlayerList(DrawContext context, TextRenderer tr,
                                         List<SyncAllPlayersPayload.PlayerInfo> players,
                                         int startX, int startY, int itemHeight) {
        EventMod.LOGGER.trace("{}Отрисовка {} игроков", LOG_PREFIX, players.size());

        for (int i = 0; i < players.size(); i++) {
            var info = players.get(i);
            String name = info.name(); // только имя, без иконок
            int color = getPlayerColor(info.teamName());
            int textY = startY + i * itemHeight;

            context.drawText(tr, Text.literal(name), startX, textY, color, false);

            EventMod.LOGGER.trace("{}  Отрисован игрок {}: '{}' цвет: {}",
                    LOG_PREFIX, i, name, String.format("0x%06X", color));
        }
    }

    /**
     * Получает цвет игрока на основе его команды.
     * @param teamName название команды
     * @return цвет в формате RGB
     */
    private static int getPlayerColor(String teamName) {
        switch (teamName) {
            case "RED":
                EventMod.LOGGER.trace("{}Цвет для команды RED: 0xFF5555", LOG_PREFIX);
                return 0xFF5555;   // красный
            case "BLUE":
                EventMod.LOGGER.trace("{}Цвет для команды BLUE: 0x5555FF", LOG_PREFIX);
                return 0x5555FF;  // синий
            default:
                EventMod.LOGGER.trace("{}Цвет для команды без названия: 0xFFFFFF", LOG_PREFIX);
                return 0xFFFFFF;      // белый
        }
    }

    /**
     * Проверяет, должен ли отображаться HUD списка игроков.
     * @return true если HUD должен отображаться
     */
    public static boolean shouldRender() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null &&
                !client.options.hudHidden &&
                !ClientPlayerList.getPlayers().isEmpty();
    }

    /**
     * Получает количество отображаемых игроков.
     * @return количество игроков
     */
    public static int getPlayerCount() {
        return ClientPlayerList.getPlayers().size();
    }

    /**
     * Получает статистику по командам.
     * @return строка со статистикой
     */
    public static String getTeamStats() {
        var players = ClientPlayerList.getPlayers();
        long redCount = players.stream().filter(p -> "RED".equals(p.teamName())).count();
        long blueCount = players.stream().filter(p -> "BLUE".equals(p.teamName())).count();
        long noTeamCount = players.stream().filter(p -> !"RED".equals(p.teamName()) && !"BLUE".equals(p.teamName())).count();

        return String.format("Teams: RED=%d, BLUE=%d, NONE=%d", redCount, blueCount, noTeamCount);
    }
}