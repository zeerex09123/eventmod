// src/main/java/ru/rebey/eventmod/hud/ClassHudRenderer.java
package ru.rebey.eventmod.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.rebey.eventmod.data.ClientPlayerData;
import ru.rebey.eventmod.data.PlayerClass;

public class ClassHudRenderer {
    public static void register() {
        HudRenderCallback.EVENT.register(ClassHudRenderer::render);
    }

    private static void render(DrawContext context, RenderTickCounter counter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        PlayerClass pc = ClientPlayerData.getPlayerClass();
        if (pc == null) return;

        String classText = switch (pc) {
            case TANK -> "🛡️ Танк";
            case ASSASSIN -> "🗡️ Ассасин";
            case ENGINEER -> "⚙️ Инженер";
            case MAGE -> "🔮 Маг";
            case SCOUT -> "👁️ Разведчик";
            default -> "❓";
        };

        String teamColor = ClientPlayerData.getTeamColor();
        Formatting fmt = "red".equals(teamColor) ? Formatting.RED :
                "blue".equals(teamColor) ? Formatting.BLUE : Formatting.WHITE;

        Text text = Text.literal(classText).formatted(fmt);
        TextRenderer tr = client.textRenderer;

        // Позиция: по центру, над хотбаром
        int x = (context.getScaledWindowWidth() - tr.getWidth(text)) / 2;
        int y = context.getScaledWindowHeight() - 55; // над хотбаром

        context.drawText(tr, text, x, y, 0xFFFFFF, true);
    }
}