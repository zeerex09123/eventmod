// src/main/java/ru/rebey/eventmod/gui/CardSelectionScreen.java
package ru.rebey.eventmod.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.network.SelectCardPayload;

public class CardSelectionScreen extends Screen {
    private final Text card1Text;
    private final Text card2Text;
    private final String card1Id;
    private final String card2Id;

    public CardSelectionScreen(Text card1Text, Text card2Text, String card1Id, String card2Id) {
        super(Text.literal("Выберите карточку"));
        this.card1Text = card1Text;
        this.card2Text = card2Text;
        this.card1Id = card1Id;
        this.card2Id = card2Id;
    }

    @Override
    protected void init() {
        int buttonWidth = 100;
        int centerX = this.width / 2;
        int leftCenter = centerX - 130;
        int rightCenter = centerX + 30;

        addDrawableChild(ButtonWidget.builder(Text.literal("✅ Выбрать").formatted(Formatting.GREEN), button -> {
            EventMod.LOGGER.info("Client: Selected card with ID: {}", card1Id);
            ClientPlayNetworking.send(new SelectCardPayload(card1Id));
            this.close();
        }).position(leftCenter - buttonWidth / 2, 140).size(buttonWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("✅ Выбрать").formatted(Formatting.GREEN), button -> {
            EventMod.LOGGER.info("Client: Selected card with ID: {}", card2Id);
            ClientPlayNetworking.send(new SelectCardPayload(card2Id));
            this.close();
        }).position(rightCenter - buttonWidth / 2, 140).size(buttonWidth, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "🃏 Выберите одну карточку", this.width / 2, 20, 0xFFFFFF);

        int centerX = this.width / 2;
        int leftCenter = centerX - 130;
        int rightCenter = centerX + 30;
        int textY = 60;
        int maxWidth = 200;

        drawCenteredTextMultiline(context, this.card1Text, leftCenter, textY, maxWidth, 0xFFFFFF);
        drawCenteredTextMultiline(context, this.card2Text, rightCenter, textY, maxWidth, 0xFFFFFF);
    }

    private void drawCenteredTextMultiline(DrawContext context, Text text, int centerX, int y, int maxWidth, int color) {
        String str = text.getString();
        String[] lines = str.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            while (this.textRenderer.getWidth(line) > maxWidth && line.length() > 0) {
                line = line.substring(0, line.length() - 1);
            }
            int x = centerX - this.textRenderer.getWidth(line) / 2;
            context.drawTextWithShadow(this.textRenderer, line, x, y + i * 12, color);
        }
    }
}