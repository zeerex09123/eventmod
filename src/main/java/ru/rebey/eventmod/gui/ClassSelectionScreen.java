// src/main/java/ru/rebey/eventmod/gui/ClassSelectionScreen.java
package ru.rebey.eventmod.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.network.SelectClassPayload;

public class ClassSelectionScreen extends Screen {
    public ClassSelectionScreen() {
        super(Text.literal("Выберите класс"));
    }

    @Override
    protected void init() {
        int buttonWidth = 150;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int startY = 60;
        int spacing = 25;

        addDrawableChild(ButtonWidget.builder(Text.literal("🛡️ Танк (щитоносец)"), button -> {
            EventMod.LOGGER.info("Client: Sending class selection - tank");
            ClientPlayNetworking.send(new SelectClassPayload("tank"));
            this.close();
        }).position(centerX - buttonWidth / 2, startY).size(buttonWidth, buttonHeight).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("🗡️ Убийца (ассасин)"), button -> {
            EventMod.LOGGER.info("Client: Sending class selection - assassin");
            ClientPlayNetworking.send(new SelectClassPayload("assassin"));
            this.close();
        }).position(centerX - buttonWidth / 2, startY + spacing).size(buttonWidth, buttonHeight).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("⚙️ Инженер (строитель)"), button -> {
            EventMod.LOGGER.info("Client: Sending class selection - engineer");
            ClientPlayNetworking.send(new SelectClassPayload("engineer"));
            this.close();
        }).position(centerX - buttonWidth / 2, startY + 2 * spacing).size(buttonWidth, buttonHeight).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("🔮 Маг"), button -> {
            EventMod.LOGGER.info("Client: Sending class selection - mage");
            ClientPlayNetworking.send(new SelectClassPayload("mage"));
            this.close();
        }).position(centerX - buttonWidth / 2, startY + 3 * spacing).size(buttonWidth, buttonHeight).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("👁️ Разведчик (скаут)"), button -> {
            EventMod.LOGGER.info("Client: Sending class selection - scout");
            ClientPlayNetworking.send(new SelectClassPayload("scout"));
            this.close();
        }).position(centerX - buttonWidth / 2, startY + 4 * spacing).size(buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "Выберите ваш класс", this.width / 2, 20, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}