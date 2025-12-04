package ru.rebey.eventmod.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.rebey.eventmod.EventMod;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Эффект уничтожения инвентаря огнем.
 * Уничтожает случайный предмет из инвентаря, когда игрок горит или находится в лаве.
 */
public class FireInventoryDestructionEffect {
    private static final String LOG_PREFIX = "[FireInventoryDestructionEffect] ";

    private static final Set<UUID> AFFECTED_PLAYERS = new HashSet<>();
    private static final Random RANDOM = new Random();
    private static final int CHECK_INTERVAL = 20; // каждую секунду (20 тиков)

    // Размеры инвентаря для случайного выбора
    private static final int INVENTORY_SIZE = 36; // основной инвентарь (0-35)
    private static final int HOTBAR_SIZE = 9; // горячая панель (0-8)
    private static final int MAIN_INVENTORY_START = 9; // начало основного инвентаря

    static {
        EventMod.LOGGER.info("{}Инициализация эффекта уничтожения инвентаря огнем", LOG_PREFIX);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % CHECK_INTERVAL != 0) return;

            try {
                int checkedPlayers = 0;
                int destroyedItems = 0;

                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    if (AFFECTED_PLAYERS.contains(player.getUuid())) {
                        checkedPlayers++;

                        // Проверяем, горит ли игрок или находится в лаве
                        if (player.isInLava() || player.isOnFire()) {
                            destroyedItems += destroyRandomItem(player);
                        }
                    }
                }

                if (destroyedItems > 0) {
                    EventMod.LOGGER.debug("{}Проверено игроков: {}, уничтожено предметов: {}",
                            LOG_PREFIX, checkedPlayers, destroyedItems);
                }

            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при обработке эффекта уничтожения инвентаря: {}",
                        LOG_PREFIX, e.getMessage(), e);
            }
        });

        EventMod.LOGGER.info("{}Эффект уничтожения инвентаря огнем инициализирован. Проверка каждые {} тиков",
                LOG_PREFIX, CHECK_INTERVAL);
    }

    /**
     * Уничтожает случайный предмет из инвентаря игрока.
     * @param player игрок
     * @return 1 если предмет уничтожен, 0 если нет
     */
    private static int destroyRandomItem(ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        var inv = player.getInventory();

        // Выбираем случайный слот из основного инвентаря (0–35)
        int slot = RANDOM.nextInt(INVENTORY_SIZE);
        ItemStack stack = inv.getStack(slot);

        if (!stack.isEmpty()) {
            try {
                // Сохраняем информацию о предмете для лога
                String itemName = stack.getItem().getName().getString();
                int itemCount = stack.getCount();

                // Уничтожаем предмет
                inv.setStack(slot, ItemStack.EMPTY);

                // Отправляем сообщение игроку
                player.sendMessage(
                        Text.literal("🔥 Предмет уничтожен огнём!").formatted(Formatting.RED),
                        true
                );

                EventMod.LOGGER.info("{}Уничтожен предмет из слота {} у игрока {}: {} x{}",
                        LOG_PREFIX, slot, playerName, itemName, itemCount);

                // Дополнительное логирование для отладки
                if (slot < HOTBAR_SIZE) {
                    EventMod.LOGGER.debug("{}  Уничтожен предмет из горячей панели (слот {})",
                            LOG_PREFIX, slot);
                } else if (slot < MAIN_INVENTORY_START + 27) {
                    EventMod.LOGGER.debug("{}  Уничтожен предмет из основного инвентаря (слот {})",
                            LOG_PREFIX, slot - MAIN_INVENTORY_START);
                }

                return 1;
            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при уничтожении предмета у игрока {}: {}",
                        LOG_PREFIX, playerName, e.getMessage(), e);
            }
        } else {
            EventMod.LOGGER.trace("{}Слот {} у игрока {} пуст", LOG_PREFIX, slot, playerName);
        }

        return 0;
    }

    /**
     * Применяет эффект уничтожения инвентаря огнем к игроку.
     * @param player игрок для применения эффекта
     */
    public static void apply(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        if (AFFECTED_PLAYERS.add(uuid)) {
            EventMod.LOGGER.info("{}Применен эффект уничтожения инвентаря огнем к игроку {}",
                    LOG_PREFIX, playerName);

            // Отправляем информационное сообщение игроку
            player.sendMessage(
                    Text.literal("⚠️ Ваш инвентарь уязвим к огню!").formatted(Formatting.YELLOW),
                    true
            );
            player.sendMessage(
                    Text.literal("  Предметы могут уничтожаться при горении").formatted(Formatting.GRAY),
                    false
            );

            EventMod.LOGGER.debug("{}Эффект применен к игроку {}. Всего игроков с эффектом: {}",
                    LOG_PREFIX, playerName, AFFECTED_PLAYERS.size());
        } else {
            EventMod.LOGGER.debug("{}Игрок {} уже имеет эффект уничтожения инвентаря",
                    LOG_PREFIX, playerName);
        }
    }

    /**
     * Удаляет эффект уничтожения инвентаря огнем у игрока.
     * @param player игрок для удаления эффекта
     */
    public static void remove(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        if (AFFECTED_PLAYERS.remove(uuid)) {
            player.sendMessage(
                    Text.literal("✅ Защита от уничтожения инвентаря восстановлена").formatted(Formatting.GREEN),
                    true
            );

            EventMod.LOGGER.info("{}Эффект уничтожения инвентаря снят с игрока {}. Осталось игроков: {}",
                    LOG_PREFIX, playerName, AFFECTED_PLAYERS.size());
        } else {
            EventMod.LOGGER.debug("{}Игрок {} не имел эффекта уничтожения инвентаря",
                    LOG_PREFIX, playerName);
        }
    }

    /**
     * Проверяет, имеет ли игрок эффект уничтожения инвентаря.
     * @param player игрок для проверки
     * @return true если эффект активен
     */
    public static boolean hasEffect(ServerPlayerEntity player) {
        boolean hasEffect = AFFECTED_PLAYERS.contains(player.getUuid());
        EventMod.LOGGER.trace("{}Проверка эффекта уничтожения инвентаря для игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), hasEffect);
        return hasEffect;
    }

    /**
     * Получает количество игроков с эффектом уничтожения инвентаря.
     * @return количество игроков
     */
    public static int getAffectedPlayerCount() {
        return AFFECTED_PLAYERS.size();
    }

    /**
     * Получает статистику эффекта.
     * @return строка со статистикой
     */
    public static String getStats() {
        return String.format("FireInventoryDestruction Stats: AffectedPlayers=%d",
                AFFECTED_PLAYERS.size());
    }
}