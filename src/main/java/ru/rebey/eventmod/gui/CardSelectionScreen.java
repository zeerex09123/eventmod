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

/**
 * Экран выбора карточек для игрока.
 * Отображает две случайные карточки на выбор.
 */
public class CardSelectionScreen extends Screen {
    private static final String LOG_PREFIX = "[CardSelectionScreen] ";

    private final Text card1Text;
    private final Text card2Text;
    private final String card1Id;
    private final String card2Id;

    /**
     * Создает экран выбора карточек.
     * @param card1Text текст первой карточки
     * @param card2Text текст второй карточки
     * @param card1Id ID первой карточки
     * @param card2Id ID второй карточки
     */
    public CardSelectionScreen(Text card1Text, Text card2Text, String card1Id, String card2Id) {
        super(Text.literal("Выберите карточку"));
        this.card1Text = card1Text;
        this.card2Text = card2Text;
        this.card1Id = card1Id;
        this.card2Id = card2Id;

        EventMod.LOGGER.info("{}Создание экрана выбора карточек. Карточка 1: {}, Карточка 2: {}",
                LOG_PREFIX, card1Id, card2Id);
    }

    /**
     * Инициализирует элементы интерфейса.
     */
    @Override
    protected void init() {
        EventMod.LOGGER.debug("{}Инициализация экрана выбора карточек", LOG_PREFIX);

        int buttonWidth = 100;
        int centerX = this.width / 2;
        int leftCenter = centerX - 130;
        int rightCenter = centerX + 30;
        int buttonY = 140;

        // Кнопка выбора первой карточки
        addDrawableChild(ButtonWidget.builder(
                        Text.literal("✅ Выбрать").formatted(Formatting.GREEN),
                        button -> {
                            EventMod.LOGGER.info("{}Клиент: выбрана карточка с ID: {}", LOG_PREFIX, card1Id);
                            try {
                                ClientPlayNetworking.send(new SelectCardPayload(card1Id));
                                EventMod.LOGGER.debug("{}Отправлен пакет выбора карточки на сервер: {}",
                                        LOG_PREFIX, card1Id);
                            } catch (Exception e) {
                                EventMod.LOGGER.error("{}Ошибка при отправке выбора карточки: {}",
                                        LOG_PREFIX, e.getMessage(), e);
                            }
                            this.close();
                        })
                .position(leftCenter - buttonWidth / 2, buttonY)
                .size(buttonWidth, 20)
                .build());

        // Кнопка выбора второй карточки
        addDrawableChild(ButtonWidget.builder(
                        Text.literal("✅ Выбрать").formatted(Formatting.GREEN),
                        button -> {
                            EventMod.LOGGER.info("{}Клиент: выбрана карточка с ID: {}", LOG_PREFIX, card2Id);
                            try {
                                ClientPlayNetworking.send(new SelectCardPayload(card2Id));
                                EventMod.LOGGER.debug("{}Отправлен пакет выбора карточки на сервер: {}",
                                        LOG_PREFIX, card2Id);
                            } catch (Exception e) {
                                EventMod.LOGGER.error("{}Ошибка при отправке выбора карточки: {}",
                                        LOG_PREFIX, e.getMessage(), e);
                            }
                            this.close();
                        })
                .position(rightCenter - buttonWidth / 2, buttonY)
                .size(buttonWidth, 20)
                .build());

        EventMod.LOGGER.debug("{}Элементы интерфейса инициализированы. Размер окна: {}x{}",
                LOG_PREFIX, this.width, this.height);
    }

    /**
     * Отрисовывает содержимое экрана.
     * @param context контекст отрисовки
     * @param mouseX координата X мыши
     * @param mouseY координата Y мыши
     * @param delta время между кадрами
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        EventMod.LOGGER.trace("{}Отрисовка экрана выбора карточек", LOG_PREFIX);

        // Отрисовываем фон
        super.render(context, mouseX, mouseY, delta);

        // Заголовок
        Text title = Text.literal("🃏 Выберите одну карточку");
        context.drawCenteredTextWithShadow(this.textRenderer, title, this.width / 2, 20, 0xFFFFFF);

        // Позиции для карточек
        int centerX = this.width / 2;
        int leftCenter = centerX - 130;
        int rightCenter = centerX + 30;
        int textY = 60;
        int maxWidth = 200;

        // Отрисовываем текст карточек
        drawCenteredTextMultiline(context, this.card1Text, leftCenter, textY, maxWidth, 0xFFFFFF);
        drawCenteredTextMultiline(context, this.card2Text, rightCenter, textY, maxWidth, 0xFFFFFF);

        EventMod.LOGGER.trace("{}Экран выбора карточек отрисован", LOG_PREFIX);
    }

    /**
     * Отрисовывает многострочный текст по центру.
     * @param context контекст отрисовки
     * @param text текст для отрисовки
     * @param centerX центр по X
     * @param y позиция Y
     * @param maxWidth максимальная ширина строки
     * @param color цвет текста
     */
    private void drawCenteredTextMultiline(DrawContext context, Text text, int centerX, int y, int maxWidth, int color) {
        String str = text.getString();
        String[] lines = str.split("\n");

        EventMod.LOGGER.trace("{}Отрисовка многострочного текста: {} строк", LOG_PREFIX, lines.length);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Обрезаем строку если она слишком длинная
            while (this.textRenderer.getWidth(line) > maxWidth && line.length() > 0) {
                line = line.substring(0, line.length() - 1);
                EventMod.LOGGER.trace("{}  Обрезана строка {}: {}", LOG_PREFIX, i, line);
            }

            int x = centerX - this.textRenderer.getWidth(line) / 2;
            context.drawTextWithShadow(this.textRenderer, line, x, y + i * 12, color);

            EventMod.LOGGER.trace("{}  Отрисована строка {}: '{}' в позиции ({}, {})",
                    LOG_PREFIX, i, line, x, y + i * 12);
        }
    }

    /**
     * Закрывает экран.
     */
    @Override
    public void close() {
        EventMod.LOGGER.info("{}Закрытие экрана выбора карточек", LOG_PREFIX);
        super.close();
    }

    /**
     * Обрабатывает нажатие клавиши ESC.
     * @param keyCode код клавиши
     * @param scanCode скан-код
     * @param modifiers модификаторы
     * @return true если клавиша обработана
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        EventMod.LOGGER.debug("{}Нажата клавиша: {}", LOG_PREFIX, keyCode);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Проверяет, нужно ли паузировать игру при открытии экрана.
     * @return true если игра должна быть на паузе
     */
    @Override
    public boolean shouldPause() {
        return false; // Не ставим игру на паузу при выборе карточки
    }
}