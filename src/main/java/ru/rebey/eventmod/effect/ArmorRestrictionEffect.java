package ru.rebey.eventmod.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.rebey.eventmod.EventMod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Эффект ограничения брони.
 * Ограничивает игроков ношением только слабой брони (не сильнее кольчужной).
 */
public class ArmorRestrictionEffect {
    private static final String LOG_PREFIX = "[ArmorRestrictionEffect] ";

    private static final Set<UUID> AFFECTED_PLAYERS = new HashSet<>();
    private static final int CHECK_INTERVAL = 20; // проверка каждую секунду (20 тиков)

    // Кэш для защиты брони по слотам
    private static final Map<String, Float[]> ARMOR_DEFENSE_CACHE = new HashMap<>();

    // Максимально разрешенная защита по слотам (как у кольчуги)
    private static final float[] MAX_ALLOWED_DEFENSE = {2.0f, 5.0f, 4.0f, 1.0f}; // шлем, нагрудник, штаны, ботинки

    static {
        EventMod.LOGGER.info("{}Инициализация эффекта ограничения брони", LOG_PREFIX);

        // Предварительно кэшируем ванильную броню
        cacheVanillaArmor();

        // Регистрируем тиковый обработчик
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % CHECK_INTERVAL != 0) return;

            try {
                int checkedPlayers = 0;
                int removedItems = 0;

                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    if (AFFECTED_PLAYERS.contains(player.getUuid())) {
                        checkedPlayers++;
                        removedItems += checkAndRemoveForbiddenArmor(player);
                    }
                }

                if (checkedPlayers > 0) {
                    EventMod.LOGGER.trace("{}Проверено игроков: {}, удалено предметов: {}",
                            LOG_PREFIX, checkedPlayers, removedItems);
                }
            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при проверке брони: {}", LOG_PREFIX, e.getMessage(), e);
            }
        });

        EventMod.LOGGER.debug("{}Эффект ограничения брони инициализирован. Проверка каждые {} тиков",
                LOG_PREFIX, CHECK_INTERVAL);
    }

    /**
     * Кэширует защиту ванильной брони.
     */
    private static void cacheVanillaArmor() {
        EventMod.LOGGER.debug("{}Кэширование защиты ванильной брони", LOG_PREFIX);

        // Кожа
        cacheArmorDefense(Items.LEATHER_HELMET, 1.0f, 1.0f, 1.0f, 1.0f);
        cacheArmorDefense(Items.LEATHER_CHESTPLATE, 3.0f, 3.0f, 3.0f, 3.0f);
        cacheArmorDefense(Items.LEATHER_LEGGINGS, 2.0f, 2.0f, 2.0f, 2.0f);
        cacheArmorDefense(Items.LEATHER_BOOTS, 1.0f, 1.0f, 1.0f, 1.0f);

        // Золото
        cacheArmorDefense(Items.GOLDEN_HELMET, 2.0f, 2.0f, 2.0f, 2.0f);
        cacheArmorDefense(Items.GOLDEN_CHESTPLATE, 5.0f, 5.0f, 5.0f, 5.0f);
        cacheArmorDefense(Items.GOLDEN_LEGGINGS, 3.0f, 3.0f, 3.0f, 3.0f);
        cacheArmorDefense(Items.GOLDEN_BOOTS, 1.0f, 1.0f, 1.0f, 1.0f);

        // Кольчуга
        cacheArmorDefense(Items.CHAINMAIL_HELMET, 2.0f, 2.0f, 2.0f, 2.0f);
        cacheArmorDefense(Items.CHAINMAIL_CHESTPLATE, 5.0f, 5.0f, 5.0f, 5.0f);
        cacheArmorDefense(Items.CHAINMAIL_LEGGINGS, 4.0f, 4.0f, 4.0f, 4.0f);
        cacheArmorDefense(Items.CHAINMAIL_BOOTS, 1.0f, 1.0f, 1.0f, 1.0f);

        // Железо (запрещено)
        cacheArmorDefense(Items.IRON_HELMET, 2.0f, 2.0f, 2.0f, 2.0f);
        cacheArmorDefense(Items.IRON_CHESTPLATE, 6.0f, 6.0f, 6.0f, 6.0f);
        cacheArmorDefense(Items.IRON_LEGGINGS, 5.0f, 5.0f, 5.0f, 5.0f);
        cacheArmorDefense(Items.IRON_BOOTS, 2.0f, 2.0f, 2.0f, 2.0f);

        // Алмаз (запрещено)
        cacheArmorDefense(Items.DIAMOND_HELMET, 3.0f, 3.0f, 3.0f, 3.0f);
        cacheArmorDefense(Items.DIAMOND_CHESTPLATE, 8.0f, 8.0f, 8.0f, 8.0f);
        cacheArmorDefense(Items.DIAMOND_LEGGINGS, 6.0f, 6.0f, 6.0f, 6.0f);
        cacheArmorDefense(Items.DIAMOND_BOOTS, 3.0f, 3.0f, 3.0f, 3.0f);

        // Незерит (запрещено)
        cacheArmorDefense(Items.NETHERITE_HELMET, 3.0f, 3.0f, 3.0f, 3.0f);
        cacheArmorDefense(Items.NETHERITE_CHESTPLATE, 8.0f, 8.0f, 8.0f, 8.0f);
        cacheArmorDefense(Items.NETHERITE_LEGGINGS, 6.0f, 6.0f, 6.0f, 6.0f);
        cacheArmorDefense(Items.NETHERITE_BOOTS, 3.0f, 3.0f, 3.0f, 3.0f);

        // Черепаший панцирь
        cacheArmorDefense(Items.TURTLE_HELMET, 2.0f, 2.0f, 2.0f, 2.0f);

        EventMod.LOGGER.debug("{}Закеширована защита для {} предметов брони",
                LOG_PREFIX, ARMOR_DEFENSE_CACHE.size());
    }

    /**
     * Кэширует защиту брони.
     * @param item предмет брони
     * @param helmetDef защита шлема
     * @param chestDef защита нагрудника
     * @param legsDef защита штанов
     * @param bootsDef защита ботинок
     */
    private static void cacheArmorDefense(Item item, float helmetDef, float chestDef,
                                          float legsDef, float bootsDef) {
        ARMOR_DEFENSE_CACHE.put(item.toString(), new Float[]{helmetDef, chestDef, legsDef, bootsDef});
        EventMod.LOGGER.trace("{}Кэширована защита для {}: [{}, {}, {}, {}]",
                LOG_PREFIX, item.getName().getString(), helmetDef, chestDef, legsDef, bootsDef);
    }

    /**
     * Проверяет и удаляет запрещенную броню у игрока.
     * @param player игрок для проверки
     * @return количество удаленных предметов
     */
    private static int checkAndRemoveForbiddenArmor(ServerPlayerEntity player) {
        int removedCount = 0;
        var inv = player.getInventory();
        String playerName = player.getName().getString();

        for (int slot = 36; slot <= 39; slot++) { // слоты брони: 36-шлем, 37-нагрудник, 38-штаны, 39-ботинки
            ItemStack stack = inv.getStack(slot);
            if (!stack.isEmpty() && isForbiddenArmor(stack, slot - 36)) {
                try {
                    // Выбрасываем предмет
                    player.dropItem(stack, true, true);
                    inv.setStack(slot, ItemStack.EMPTY);
                    removedCount++;

                    EventMod.LOGGER.info("{}Удалена запрещенная броня у игрока {}: {} в слоте {}",
                            LOG_PREFIX, playerName, stack.getItem().getName().getString(), slot - 36);

                } catch (Exception e) {
                    EventMod.LOGGER.error("{}Ошибка при удалении брони у игрока {}: {}",
                            LOG_PREFIX, playerName, e.getMessage(), e);
                }
            }
        }

        if (removedCount > 0) {
            player.sendMessage(
                    Text.literal("❌ Запрещена броня сильнее кольчужной!").formatted(Formatting.RED),
                    true
            );
            EventMod.LOGGER.debug("{}Игроку {} отправлено предупреждение об удалении брони",
                    LOG_PREFIX, playerName);
        }

        return removedCount;
    }

    /**
     * Проверяет, является ли броня запрещенной.
     * @param stack предмет для проверки
     * @param slotIndex индекс слота (0-шлем, 1-нагрудник, 2-штаны, 3-ботинки)
     * @return true если броня запрещена
     */
    private static boolean isForbiddenArmor(ItemStack stack, int slotIndex) {
        if (!(stack.getItem() instanceof ArmorItem)) {
            return false;
        }

        float defense = getArmorDefense(stack.getItem(), slotIndex);
        float maxAllowed = MAX_ALLOWED_DEFENSE[slotIndex];

        EventMod.LOGGER.trace("{}Проверка брони: {} в слоте {}, защита: {}, максимум: {}",
                LOG_PREFIX, stack.getItem().getName().getString(), slotIndex, defense, maxAllowed);

        return defense > maxAllowed;
    }

    /**
     * Получает защиту брони.
     * @param item предмет брони
     * @param slotIndex индекс слота
     * @return значение защиты
     */
    private static float getArmorDefense(Item item, int slotIndex) {
        String itemKey = item.toString();

        // Проверяем кэш
        if (ARMOR_DEFENSE_CACHE.containsKey(itemKey)) {
            return ARMOR_DEFENSE_CACHE.get(itemKey)[slotIndex];
        }

        // Для модовой брони вычисляем защиту
        float defense = calculateModArmorDefense(item, slotIndex);

        // Кэшируем результат
        Float[] defenses = new Float[4];
        for (int i = 0; i < 4; i++) {
            defenses[i] = calculateModArmorDefense(item, i);
        }
        ARMOR_DEFENSE_CACHE.put(itemKey, defenses);

        EventMod.LOGGER.debug("{}Кэширована защита модовой брони: {} -> {} в слоте {}",
                LOG_PREFIX, item.getName().getString(), defense, slotIndex);

        return defense;
    }

    /**
     * Вычисляет защиту модовой брони.
     * @param item предмет брони
     * @param slotIndex индекс слота
     * @return значение защиты
     */
    private static float calculateModArmorDefense(Item item, int slotIndex) {
        if (item instanceof ArmorItem armorItem) {
            try {
                // В Minecraft 1.21 getMaterial() возвращает RegistryEntry<ArmorMaterial>
                var materialEntry = armorItem.getMaterial();

                // Получаем материал из RegistryEntry
                if (materialEntry.hasKeyAndValue()) {
                    ArmorMaterial material = materialEntry.value();

                    // Определяем тип брони для слота
                    ArmorItem.Type type = switch (slotIndex) {
                        case 0 -> ArmorItem.Type.HELMET;
                        case 1 -> ArmorItem.Type.CHESTPLATE;
                        case 2 -> ArmorItem.Type.LEGGINGS;
                        case 3 -> ArmorItem.Type.BOOTS;
                        default -> ArmorItem.Type.BOOTS;
                    };

                    // Получаем защиту из материала
                    int defenseValue = material.getProtection(type);

                    EventMod.LOGGER.trace("{}Вычислена защита модовой брони: {} тип {} = {}",
                            LOG_PREFIX, item.getName().getString(), type.getName(), defenseValue);

                    return defenseValue;
                }
            } catch (Exception e) {
                EventMod.LOGGER.error("{}Ошибка при вычислении защиты модовой брони {}: {}",
                        LOG_PREFIX, item.getName().getString(), e.getMessage(), e);
            }
        }
        return 0.0f;
    }

    /**
     * Применяет эффект ограничения брони к игроку.
     * @param player игрок
     */
    public static void apply(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        if (AFFECTED_PLAYERS.add(uuid)) {
            EventMod.LOGGER.info("{}Применение эффекта ограничения брони к игроку {}",
                    LOG_PREFIX, playerName);

            // Проверяем текущую броню
            checkAndRemoveForbiddenArmor(player);

            // Отправляем сообщения игроку
            player.sendMessage(
                    Text.literal("⚠️ Ограничение брони: не сильнее кольчужной").formatted(Formatting.YELLOW),
                    true
            );
            player.sendMessage(
                    Text.literal("  Разрешено: кожа, золото, кольчуга").formatted(Formatting.GRAY),
                    false
            );

            EventMod.LOGGER.debug("{}Эффект применен к игроку {}. Всего игроков с эффектом: {}",
                    LOG_PREFIX, playerName, AFFECTED_PLAYERS.size());
        } else {
            EventMod.LOGGER.debug("{}Игрок {} уже имеет эффект ограничения брони",
                    LOG_PREFIX, playerName);
        }
    }

    /**
     * Удаляет эффект ограничения брони у игрока.
     * @param player игрок
     */
    public static void remove(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        String playerName = player.getName().getString();

        if (AFFECTED_PLAYERS.remove(uuid)) {
            player.sendMessage(
                    Text.literal("✅ Ограничение брони снято").formatted(Formatting.GREEN),
                    true
            );

            EventMod.LOGGER.info("{}Эффект ограничения брони снят с игрока {}. Осталось игроков: {}",
                    LOG_PREFIX, playerName, AFFECTED_PLAYERS.size());
        } else {
            EventMod.LOGGER.debug("{}Игрок {} не имел эффекта ограничения брони",
                    LOG_PREFIX, playerName);
        }
    }

    /**
     * Проверяет, имеет ли игрок эффект ограничения брони.
     * @param player игрок
     * @return true если эффект активен
     */
    public static boolean hasEffect(ServerPlayerEntity player) {
        boolean hasEffect = AFFECTED_PLAYERS.contains(player.getUuid());
        EventMod.LOGGER.trace("{}Проверка эффекта ограничения брони для игрока {}: {}",
                LOG_PREFIX, player.getName().getString(), hasEffect);
        return hasEffect;
    }

    /**
     * Получает количество игроков с эффектом ограничения брони.
     * @return количество игроков
     */
    public static int getAffectedPlayerCount() {
        return AFFECTED_PLAYERS.size();
    }
}