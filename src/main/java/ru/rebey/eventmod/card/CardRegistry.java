// src/main/java/ru/rebey/eventmod/card/CardRegistry.java
package ru.rebey.eventmod.card;

import net.minecraft.text.Text;
import ru.rebey.eventmod.data.PlayerClass;

import java.util.HashMap;
import java.util.Map;

public class CardRegistry {

    // 🔑 Ключевое: public static class
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

    private static final Map<PlayerClass, CardOption[]> CARDS = new HashMap<>();

    static {
        // Пример для Танка
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

        // Добавь остальные классы по аналогии
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
    }

    public static CardOption[] getCardsFor(PlayerClass playerClass) {
        return CARDS.getOrDefault(playerClass, new CardOption[0]);
    }
}