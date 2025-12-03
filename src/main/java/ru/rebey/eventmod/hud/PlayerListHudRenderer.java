// src/main/java/ru/rebey/eventmod/hud/PlayerListHudRenderer.java
package ru.rebey.eventmod.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.rebey.eventmod.data.ClientPlayerList;
import ru.rebey.eventmod.network.SyncAllPlayersPayload;

import java.util.ArrayList;
import java.util.List;

public class PlayerListHudRenderer {
    public static void register() {
        HudRenderCallback.EVENT.register(PlayerListHudRenderer::render);
    }

    private static void render(DrawContext context, RenderTickCounter counter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options.hudHidden) return;

        var players = ClientPlayerList.getPlayers(); // ← убедись, что имя класса ClientPlayerList
        if (players.isEmpty()) return;

        TextRenderer tr = client.textRenderer;
        int padding = 6;
        int lineHeight = 12;
        int itemHeight = lineHeight + 2;

        // Группировка по командам
        List<SyncAllPlayersPayload.PlayerInfo> red = new ArrayList<>();
        List<SyncAllPlayersPayload.PlayerInfo> blue = new ArrayList<>();
        List<SyncAllPlayersPayload.PlayerInfo> none = new ArrayList<>();

        for (var info : players) {
            String team = info.teamName();
            if ("RED".equals(team)) {
                red.add(info);
            } else if ("BLUE".equals(team)) {
                blue.add(info);
            } else {
                none.add(info);
            }
        }

        List<SyncAllPlayersPayload.PlayerInfo> sorted = new ArrayList<>();
        sorted.addAll(red);
        sorted.addAll(blue);
        sorted.addAll(none);

        if (sorted.isEmpty()) return;

        // Заголовок
        Text headerText = Text.literal("👥 Игроки").formatted(Formatting.BOLD, Formatting.YELLOW);
        int headerWidth = tr.getWidth(headerText);
        int contentWidth = headerWidth;

        // Находим максимальную ширину НИКА (без иконок!)
        for (var info : sorted) {
            int w = tr.getWidth(info.name()); // ← только имя
            if (w > contentWidth) contentWidth = w;
        }

        int boxWidth = contentWidth + padding * 2;
        int boxHeight = padding * 2 + lineHeight + 4 + sorted.size() * itemHeight;

        int x = context.getScaledWindowWidth() - boxWidth - 10;
        int y = (context.getScaledWindowHeight() - boxHeight) / 2;

        // Фон и рамка
        context.fill(x - 2, y - 2, x + boxWidth + 2, y + boxHeight + 2, 0x90000000);
        context.drawBorder(x - 2, y - 2, x + boxWidth + 2, y + boxHeight + 2, 0xFF8000);

        // Заголовок
        context.drawText(tr, headerText, x + padding, y + padding, 0xFFFFFF, false);

        // Только ники (без классов)
        int startY = y + padding + lineHeight + 4;
        for (int i = 0; i < sorted.size(); i++) {
            var info = sorted.get(i);
            String name = info.name(); // ← без иконки класса
            int color = getPlayerColor(info.teamName());
            int textY = startY + i * itemHeight;
            context.drawText(tr, Text.literal(name), x + padding, textY, color, false);
        }
    }

    private static int getPlayerColor(String teamName) {
        return switch (teamName) {
            case "RED" -> 0xFF5555;   // красный
            case "BLUE" -> 0x5555FF;  // синий
            default -> 0xFFFFFF;      // белый
        };
    }
}