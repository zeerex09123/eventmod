package ru.rebey.eventmod.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import ru.rebey.eventmod.EventMod;
import ru.rebey.eventmod.network.SelectClassPayload;

/**
 * Экран выбора класса для игрока.
 * Предоставляет возможность выбора одного из пяти классов.
 */
public class ClassSelectionScreen extends Screen {
    private static final String LOG_PREFIX = "[ClassSelectionScreen] ";

    /**
     * Создает экран выбора класса.
     */
    public ClassSelectionScreen() {
        super(Text.literal("Выберите класс"));
        EventMod.LOGGER.info("{}Создание экрана выбора класса", LOG_PREFIX);
    }

    /**
     * Инициализирует элементы интерфейса.
     */
    @Override
    protected void init() {
        EventMod.LOGGER.debug("{}Инициализация экрана выбора класса", LOG_PREFIX);

        int buttonWidth = 150;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int startY = 60;
        int spacing = 25;

        // Кнопка выбора Танка
        addDrawableChild(ButtonWidget.builder(
                        Text.literal("🛡️ Танк (щитоносец)"),
                        button -> {
                            EventMod.LOGGER.info("{}Клиент: отправка выбора класса - танк", LOG_PREFIX);
                            try {
                                ClientPlayNetworking.send(new SelectClassPayload("tank"));
                                EventMod.LOGGER.debug("{}Отправлен пакет выбора класса на сервер: tank", LOG_PREFIX);
                            } catch (Exception e) {
                                EventMod.LOGGER.error("{}Ошибка при отправке выбора класса: {}",
                                        LOG_PREFIX, e.getMessage(), e);
                            }
                            this.close();
                        })
                .position(centerX - buttonWidth / 2, startY)
                .size(buttonWidth, buttonHeight)
                .build());

        // Кнопка выбора Убийцы
        addDrawableChild(ButtonWidget.builder(
                        Text.literal("🗡️ Убийца (ассасин)"),
                        button -> {
                            EventMod.LOGGER.info("{}Клиент: отправка выбора класса - assassin", LOG_PREFIX);
                            try {
                                ClientPlayNetworking.send(new SelectClassPayload("assassin"));
                                EventMod.LOGGER.debug("{}Отправлен пакет выбора класса на сервер: assassin", LOG_PREFIX);
                            } catch (Exception e) {
                                EventMod.LOGGER.error("{}Ошибка при отправке выбора класса: {}",
                                        LOG_PREFIX, e.getMessage(), e);
                            }
                            this.close();
                        })
                .position(centerX - buttonWidth / 2, startY + spacing)
                .size(buttonWidth, buttonHeight)
                .build());

        // Кнопка выбора Инженера
        addDrawableChild(ButtonWidget.builder(
                        Text.literal("⚙️ Инженер (строитель)"),
                        button -> {
                            EventMod.LOGGER.info("{}Клиент: отправка выбора класса - engineer", LOG_PREFIX);
                            try {
                                ClientPlayNetworking.send(new SelectClassPayload("engineer"));
                                EventMod.LOGGER.debug("{}Отправлен пакет выбора класса на сервер: engineer", LOG_PREFIX);
                            } catch (Exception e) {
                                EventMod.LOGGER.error("{}Ошибка при отправке выбора класса: {}",
                                        LOG_PREFIX, e.getMessage(), e);
                            }
                            this.close();
                        })
                .position(centerX - buttonWidth / 2, startY + 2 * spacing)
                .size(buttonWidth, buttonHeight)
                .build());

        // Кнопка выбора Мага
        addDrawableChild(ButtonWidget.builder(
                        Text.literal("🔮 Маг"),
                        button -> {
                            EventMod.LOGGER.info("{}Клиент: отправка выбора класса - mage", LOG_PREFIX);
                            try {
                                ClientPlayNetworking.send(new SelectClassPayload("mage"));
                                EventMod.LOGGER.debug("{}Отправлен пакет выбора класса на сервер: mage", LOG_PREFIX);
                            } catch (Exception e) {
                                EventMod.LOGGER.error("{}Ошибка при отправке выбора класса: {}",
                                        LOG_PREFIX, e.getMessage(), e);
                            }
                            this.close();
                        })
                .position(centerX - buttonWidth / 2, startY + 3 * spacing)
                .size(buttonWidth, buttonHeight)
                .build());

        // Кнопка выбора Разведчика
        addDrawableChild(ButtonWidget.builder(
                        Text.literal("👁️ Разведчик (скаут)"),
                        button -> {
                            EventMod.LOGGER.info("{}Клиент: отправка выбора класса - scout", LOG_PREFIX);
                            try {
                                ClientPlayNetworking.send(new SelectClassPayload("scout"));
                                EventMod.LOGGER.debug("{}Отправлен пакет выбора класса на сервер: scout", LOG_PREFIX);
                            } catch (Exception e) {
                                EventMod.LOGGER.error("{}Ошибка при отправке выбора класса: {}",
                                        LOG_PREFIX, e.getMessage(), e);
                            }
                            this.close();
                        })
                .position(centerX - buttonWidth / 2, startY + 4 * spacing)
                .size(buttonWidth, buttonHeight)
                .build());

        EventMod.LOGGER.debug("{}Элементы интерфейса инициализированы. Создано {} кнопок. Размер окна: {}x{}",
                LOG_PREFIX, 5, this.width, this.height);
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
        EventMod.LOGGER.trace("{}Отрисовка экрана выбора класса", LOG_PREFIX);

        // Отрисовываем фон
        this.renderBackground(context, mouseX, mouseY, delta);

        // Заголовок
        Text title = Text.literal("Выберите ваш класс");
        context.drawCenteredTextWithShadow(this.textRenderer, title, this.width / 2, 20, 0xFFFFFF);

        // Отрисовываем элементы интерфейса
        super.render(context, mouseX, mouseY, delta);

        EventMod.LOGGER.trace("{}Экран выбора класса отрисован", LOG_PREFIX);
    }

    /**
     * Закрывает экран.
     */
    @Override
    public void close() {
        EventMod.LOGGER.info("{}Закрытие экрана выбора класса", LOG_PREFIX);
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
        return false; // Не ставим игру на паузу при выборе класса
    }

    /**
     * Получает описание экрана.
     * @return описание экрана
     */
    @Override
    public Text getTitle() {
        return this.title;
    }
}