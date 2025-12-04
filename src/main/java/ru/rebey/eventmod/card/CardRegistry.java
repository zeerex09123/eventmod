package ru.rebey.eventmod.card;

import net.minecraft.text.Text;
import ru.rebey.eventmod.data.PlayerClass;
import ru.rebey.eventmod.EventMod;

import java.util.HashMap;
import java.util.Map;

/**
 * Реестр карточек для различных классов игроков.
 * Хранит определения карточек с их ID, названиями и описаниями эффектов.
 */
public class CardRegistry {

    /**
     * Класс, представляющий опцию карточки.
     * Содержит ID, название, описание баффа и дебаффа.
     */
    public static class CardOption {
        private final String id;
        private final Text name;
        private final Text buffDescription;
        private final Text debuffDescription;

        public CardOption(String id, Text name, Text buffDescription, Text debuffDescription) {
            this.id = id;
            this.name = name;
            this.buffDescription = buffDescription;
            this.debuffDescription = debuffDescription;
        }

        public String id() { return id; }
        public Text name() { return name; }
        public Text buffDescription() { return buffDescription; }
        public Text debuffDescription() { return debuffDescription; }
    }

    // Карта для хранения карточек по классам игроков
    private static final Map<PlayerClass, CardOption[]> CARDS = new HashMap<>();

    static {
        // Инициализация карточек для Танка
        CARDS.put(PlayerClass.TANK, new CardOption[]{
                new CardOption(
                        "tank_card_1",
                        Text.literal("🛡️ Щит души"),
                        Text.literal("+4♥"),
                        Text.literal("−30% скорости")
                ),
                new CardOption(
                        "tank_card_2",
                        Text.literal("🦾 Железные суставы"),
                        Text.literal("−50% урона от падения"),
                        Text.literal("Только топоры/кирки")
                ),
                new CardOption(
                        "tank_card_3",
                        Text.literal("🛡️ Удар щитом"),
                        Text.literal("Атака щитом отбрасывает врагов"),
                        Text.literal("Атака щитом отбрасывает врагов")
                ),
        });

        // Инициализация карточек для Ассасина
        CARDS.put(PlayerClass.ASSASSIN, new CardOption[]{
                new CardOption(
                        "assassin_card_1",
                        Text.literal("🎯 Охотник на целых"),
                        Text.literal("+50% урона по игрокам с полным здоровьем"),
                        Text.literal("−20% максимального здоровья")
                ),
                new CardOption(
                        "assassin_card_2",
                        Text.literal("👻 Призрак ночи"),
                        Text.literal("4 сек без движения → невидимость"),
                        Text.literal("Только кожаная броня")
                )
        });

        EventMod.LOGGER.info("[CardRegistry] Инициализированы карточки для {} классов", CARDS.size());
    }

    /**
     * Получает массив карточек для указанного класса игрока.
     * @param playerClass класс игрока
     * @return массив карточек или пустой массив, если карточек нет
     */
    public static CardOption[] getCardsFor(PlayerClass playerClass) {
        CardOption[] cards = CARDS.getOrDefault(playerClass, new CardOption[0]);
        EventMod.LOGGER.debug("[CardRegistry] Получено {} карточек для класса {}", cards.length, playerClass);
        return cards;
    }
}