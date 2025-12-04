package ru.rebey.eventmod.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ru.rebey.eventmod.EventMod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Эффект ограничения оружия.
 * Разрешает игрокам использовать только кирки в качестве оружия.
 */
public class WeaponRestrictionEffect {
    private static final String LOG_PREFIX = "[WeaponRestrictionEffect] ";

    private static final Set<UUID> RESTRICTED_PLAYERS = new HashSet<>();
    private static final int CHECK_INTERVAL = 20; // каждые 20 тиков = 1 секунда

    // Разрешенные предметы (только кирки)
    private static final Set<net.minecraft.item.Item> ALLOWED_WEAPONS = new HashSet<>();

    static {
        EventMod.LOGGER.info("{}Инициализация эффекта ограничения оружия", LOG_PREFIX);

        // Инициализируем список разрешенных предметов
        initializeAllowedWeapons();

        // Регистрируем тиковый обработчик
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % CHECK_INTERVAL != 0) return;

            try {
                int checkedPlayers = 0;
                int droppedItems = 0;

                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    if (RESTRICTED_PLAYERS.contains(player.getUuid())) {
                        checkedPlayers++;
                        droppedItems += checkAndDropWeapon(player);
                    }
                }

                if (droppedItems > 0) {
                    EventMod.LOGGER.debug("{}Проверено игроков: {}, выброшено предметов: {}",
                            LOG_PREFIX, checkedPlayers, droppedItems);
                }

            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при проверке ограничения оружия: {}",
                        LOG_PREFIX, e.getMessage(), e);
            }
        });

        EventMod.LOGGER.info("{}Эффект ограничения оружия инициализирован. Проверка каждые {} тиков",
                LOG_PREFIX, CHECK_INTERVAL);
    }

    /**
     * Инициализирует список разрешенных предметов.
     */
    private static void initializeAllowedWeapons() {
        // Разрешаем все кирки
        ALLOWED_WEAPONS.add(Items.WOODEN_PICKAXE);
        ALLOWED_WEAPONS.add(Items.STONE_PICKAXE);
        ALLOWED_WEAPONS.add(Items.IRON_PICKAXE);
        ALLOWED_WEAPONS.add(Items.GOLDEN_PICKAXE);
        ALLOWED_WEAPONS.add(Items.DIAMOND_PICKAXE);
        ALLOWED_WEAPONS.add(Items.NETHERITE_PICKAXE);

        EventMod.LOGGER.debug("{}Инициализировано {} разрешенных предметов",
                LOG_PREFIX, ALLOWED_WEAPONS.size());
    }

    /**
     * Проверяет и выбрасывает запрещенное оружие у игрока.
     * @param player игрок для проверки
     * @return количество выброшенных предметов
     */
    private static int checkAndDropWeapon(ServerPlayerEntity player) {
        int droppedCount = 0;
        String playerName = player.getName().getString();

        // Проверяем обе руки
        for (net.minecraft.util.Hand hand : net.minecraft.util.Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isEmpty() && !isAllowedWeapon(stack)) {
                try {
                    // Выбрасываем предмет
                    player.dropItem(stack, true, true);
                    player.setStackInHand(hand, ItemStack.EMPTY);
                    droppedCount++;

                    EventMod.LOGGER.info("{}Выброшено запрещенное оружие у игрока {}: {} в руке {}",
                            LOG_PREFIX, playerName, stack.getItem().getName().getString(),
                            hand == net.minecraft.util.Hand.MAIN_HAND ? "основной" : "второстепенной");

                } catch (Exception e) {
                    EventMod.LOGGER.error("{}Ошибка при выбросе оружия у игрока {}: {}",
                            LOG_PREFIX, playerName, e.getMessage(), e);
                }
            }
        }

        if (droppedCount > 0) {
            player.sendMessage(
                    Text.literal("❌ Запрещённое оружие выброшено!").formatted(Formatting.RED),
                    true
            );
            EventMod.LOGGER.debug("{}Игроку {} отправлено предупреждение о выбросе оружия",
                    LOG_PREFIX, playerName);
        }

        return droppedCount;
    }

    /**
     * Проверяет, является ли предмет разрешенным оружием.
     * @param stack предмет для проверки
     * @return true если предмет разрешен
     */
    private static boolean isAllowedWeapon(ItemStack stack) {
        // Если это НЕ оружие — разрешено
        if (!isWeapon(stack)) {
            return true;
        }

        // Если это оружие — разрешены ТОЛЬКО кирки
        return ALLOWED_WEAPONS.contains(stack.getItem());
    }

    /**
     * Проверяет, является ли предмет оружием.
     * @param stack предмет для проверки
     * @return true если предмет считается оружием
     */
    private static boolean isWeapon(ItemStack stack) {
        // Определяем, является ли предмет оружием по типу предмета
        return stack.getItem() instanceof net.minecraft.item.SwordItem ||
                stack.getItem() instanceof net.minecraft.item.AxeItem ||
                stack.getItem() instanceof net.minecraft.item.BowItem ||
                stack.getItem() instanceof net.minecraft.item.CrossbowItem ||
                stack.getItem() instanceof net.minecraft.item.TridentItem ||
                stack.isOf(Items.FISHING_ROD);
    }

    /**
     * Применяет эффект ограничения оружия к игроку.
     * @param player игрок для применения эффекта
     */
    public static void apply(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        if (RESTRICTED_PLAYERS.add(uuid)) {
            EventMod.LOGGER.info("{}Применение эффекта ограничения оружия к игроку {}",
                    LOG_PREFIX, playerName);

            // Сразу очищаем инвентарь от запрещённого оружия
            int cleanedItems = cleanupInventory(player);

            // Отправляем сообщения игроку
            player.sendMessage(
                    Text.literal("⚠️ Оружие ограничено: только кирки разрешены!").formatted(Formatting.YELLOW),
                    true
            );
            player.sendMessage(
                    Text.literal("  Запрещено: мечи, топоры, луки и т.д.").formatted(Formatting.GRAY),
                    false
            );

            EventMod.LOGGER.info("{}Эффект ограничения оружия применен к игроку {}. Очищено предметов: {}. Всего игроков с эффектом: {}",
                    LOG_PREFIX, playerName, cleanedItems, RESTRICTED_PLAYERS.size());
        } else {
            EventMod.LOGGER.debug("{}Игрок {} уже имеет эффект ограничения оружия",
                    LOG_PREFIX, playerName);
        }
    }

    /**
     * Очищает инвентарь игрока от запрещенного оружия.
     * @param player игрок
     * @return количество очищенных предметов
     */
    private static int cleanupInventory(ServerPlayerEntity player) {
        String playerName = player.getName().getString();
        int cleanedCount = 0;
        var inv = player.getInventory();

        EventMod.LOGGER.debug("{}Очистка инвентаря игрока {} от запрещенного оружия",
                LOG_PREFIX, playerName);

        // Проверяем все слоты (включая горячую панель и основной инвентарь)
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && !isAllowedWeapon(stack)) {
                try {
                    player.dropItem(stack, true, true);
                    inv.setStack(i, ItemStack.EMPTY);
                    cleanedCount++;

                    EventMod.LOGGER.debug("{}  Очищен слот {}: {}", LOG_PREFIX, i,
                            stack.getItem().getName().getString());
                } catch (Exception e) {
                    EventMod.LOGGER.error("{}  Ошибка при очистке слота {} у игрока {}: {}",
                            LOG_PREFIX, i, playerName, e.getMessage(), e);
                }
            }
        }

        // Проверяем off-hand
        ItemStack offhand = inv.getStack(40); // слот off-hand
        if (!offhand.isEmpty() && !isAllowedWeapon(offhand)) {
            try {
                player.dropItem(offhand, true, true);
                inv.setStack(40, ItemStack.EMPTY);
                cleanedCount++;

                EventMod.LOGGER.debug("{}  Очищен off-hand: {}", LOG_PREFIX,
                        offhand.getItem().getName().getString());
            } catch (Exception e) {
                EventMod.LOGGER.error("{}  Ошибка при очистке off-hand у игрока {}: {}",
                        LOG_PREFIX, playerName, e.getMessage(), e);
            }
        }

        EventMod.LOGGER.debug("{}Очистка инвентаря игрока {} завершена. Очищено предметов: {}",
                LOG_PREFIX, playerName, cleanedCount);

        return cleanedCount;
    }

    /**
     * Удаляет эффект ограничения оружия у игрока.
     * @param player игрок для удаления эффекта
     */
    public static void remove(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        if (RESTRICTED_PLAYERS.remove(uuid)) {
            player.sendMessage(
                    Text.literal("✅ Ограничение оружия снято").formatted(Formatting.GREEN),
                    true
            );

            EventMod.LOGGER.info("{}Эффект ограничения оружия снят с игрока {}. Осталось игроков: {}",
                    LOG_PREFIX, playerName, RESTRICTED_PLAYERS.size());
        } else {
            EventMod.LOGGER.debug("{}Игрок {} не имел эффекта ограничения оружия",
                    LOG_PREFIX, playerName);
        }
    }

    /**
     * Проверяет, имеет ли игрок эффект ограничения оружия.
     * @param player игрок для проверки
     * @return true если эффект активен
     */
    public static boolean hasEffect(ServerPlayerEntity player) {
        boolean hasEffect = RESTRICTED_PLAYERS.contains(player.getUuid());
        EventMod.LOGGER.trace("{}Проверка эффекта ограничения оружия для игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), hasEffect);
        return hasEffect;
    }

    /**
     * Получает количество игроков с эффектом ограничения оружия.
     * @return количество игроков
     */
    public static int getAffectedPlayerCount() {
        return RESTRICTED_PLAYERS.size();
    }

    /**
     * Получает список разрешенных предметов.
     * @return множество разрешенных предметов
     */
    public static Set<net.minecraft.item.Item> getAllowedWeapons() {
        return new HashSet<>(ALLOWED_WEAPONS);
    }

    /**
     * Получает статистику эффекта.
     * @return строка со статистикой
     */
    public static String getStats() {
        return String.format("WeaponRestriction Stats: AffectedPlayers=%d, AllowedWeapons=%d",
                RESTRICTED_PLAYERS.size(), ALLOWED_WEAPONS.size());
    }
}