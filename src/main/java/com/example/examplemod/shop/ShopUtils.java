package com.example.examplemod.shop;

import com.example.examplemod.item.ModItems;
import com.example.examplemod.item.Rarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;

import java.util.Random;

public class ShopUtils {

    public static int calcSellPrice(ItemStack stack) {
        CompoundNBT nbt = stack.getTag();
        if (nbt == null || !nbt.contains("ItemLevel")) return 5;
        int level = nbt.getInt("ItemLevel");
        int rarity = Math.min(4, nbt.getInt("RarityLevel"));
        int stats = nbt.getInt("StatStrength") + nbt.getInt("StatDexterity")
                + nbt.getInt("StatIntelligence") + nbt.getInt("StatLuck")
                + nbt.getInt("StatVitality");
        float matMult = getMaterialMultiplier(stack.getItem());

        if (stack.getItem().isEdible()) {
            return (level * 5 + rarity * 50) * 2;
        }
        if (stack.getItem() instanceof com.example.examplemod.item.RpgPotionItem) {
            return (level * 5 + rarity * 20) * 2;
        }

        // НОВОЕ: Цены на продажу кристаллов
        if (stack.getItem() == ModItems.SMALL_MAGIC_CRYSTAL.get()) return (level * 2 + rarity * 10);
        if (stack.getItem() == ModItems.MEDIUM_MAGIC_CRYSTAL.get()) return (level * 5 + rarity * 25);
        if (stack.getItem() == ModItems.LARGE_MAGIC_CRYSTAL.get()) return (level * 10 + rarity * 60);

        return (int) ((level * 3 + rarity * 20 + stats * 2) * matMult);
    }

    public static int calcBuyPrice(ItemStack stack) {
        CompoundNBT nbt = stack.getOrCreateTag();
        int level = Math.max(1, nbt.getInt("ItemLevel"));
        int rarity = Math.min(4, nbt.getInt("RarityLevel"));

        if (stack.getItem().isEdible()) {
            return level * 30 + rarity * 300;
        }
        if (stack.getItem() instanceof com.example.examplemod.item.RpgPotionItem) {
            return level * 10 + rarity * 50;
        }

        // НОВОЕ: Цены на покупку кристаллов
        if (stack.getItem() == ModItems.SMALL_MAGIC_CRYSTAL.get()) return (level * 4 + rarity * 20);
        if (stack.getItem() == ModItems.MEDIUM_MAGIC_CRYSTAL.get()) return (level * 10 + rarity * 60);
        if (stack.getItem() == ModItems.LARGE_MAGIC_CRYSTAL.get()) return (level * 20 + rarity * 150);

        float matMult = getMaterialMultiplier(stack.getItem());
        int basePrice = level * 15 + rarity * 150;
        return (int) (basePrice * matMult);
    }

    public static float getMaterialMultiplier(Item item) {
        // ... (оставляем как было) ...
        if (item == Items.WOODEN_SWORD || item == Items.LEATHER_HELMET
                || item == Items.LEATHER_CHESTPLATE || item == Items.LEATHER_LEGGINGS
                || item == Items.LEATHER_BOOTS) return 1.0f;
        if (item == Items.STONE_SWORD) return 1.5f;
        if (item == Items.IRON_SWORD || item == Items.IRON_HELMET
                || item == Items.IRON_CHESTPLATE || item == Items.IRON_LEGGINGS
                || item == Items.IRON_BOOTS) return 2.5f;
        if (item == Items.GOLDEN_SWORD || item == Items.GOLDEN_HELMET
                || item == Items.GOLDEN_CHESTPLATE || item == Items.GOLDEN_LEGGINGS
                || item == Items.GOLDEN_BOOTS) return 3.5f;
        if (item == Items.DIAMOND_SWORD || item == Items.DIAMOND_HELMET
                || item == Items.DIAMOND_CHESTPLATE || item == Items.DIAMOND_LEGGINGS
                || item == Items.DIAMOND_BOOTS) return 5.0f;
        // ... (и так далее для твоих слитков) ...
        return 1.0f;
    }

    private static Item pickFrom(Random rand, Item... items) {
        return items[rand.nextInt(items.length)];
    }

    private static Item pickItemForMerchant(Random rand, int merchantType) {
        if (merchantType == 0) {
            return pickFrom(rand, Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, ModItems.SILVER_SWORD.get(), ModItems.COBALT_SWORD.get(), ModItems.MITHRIL_SWORD.get(), ModItems.ADAMANTIUM_SWORD.get(), ModItems.ORICHALCUM_SWORD.get());
        } else if (merchantType == 1) {
            return pickFrom(rand, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, ModItems.SILVER_HELMET.get(), ModItems.COBALT_HELMET.get(), ModItems.MITHRIL_HELMET.get(), ModItems.ADAMANTIUM_HELMET.get(), ModItems.ORICHALCUM_HELMET.get(), ModItems.SILVER_CHESTPLATE.get(), ModItems.COBALT_CHESTPLATE.get(), ModItems.MITHRIL_CHESTPLATE.get(), ModItems.ADAMANTIUM_CHESTPLATE.get(), ModItems.ORICHALCUM_CHESTPLATE.get(), ModItems.SILVER_LEGGINGS.get(), ModItems.COBALT_LEGGINGS.get(), ModItems.MITHRIL_LEGGINGS.get(), ModItems.ADAMANTIUM_LEGGINGS.get(), ModItems.ORICHALCUM_LEGGINGS.get(), ModItems.SILVER_BOOTS.get(), ModItems.COBALT_BOOTS.get(), ModItems.MITHRIL_BOOTS.get(), ModItems.ADAMANTIUM_BOOTS.get(), ModItems.ORICHALCUM_BOOTS.get());
        } else if (merchantType == 2) {
            // НОВОЕ: Добавляем кристаллы Магу
            return pickFrom(rand, ModItems.MANA_POTION.get(), ModItems.HEALTH_POTION.get(), ModItems.SMALL_MAGIC_CRYSTAL.get(), ModItems.MEDIUM_MAGIC_CRYSTAL.get(), ModItems.LARGE_MAGIC_CRYSTAL.get());
        } else {
            return pickFrom(rand, Items.BREAD, Items.COOKIE, Items.BAKED_POTATO, Items.PUMPKIN_PIE, Items.MUSHROOM_STEW, Items.BEETROOT_SOUP, Items.RABBIT_STEW, Items.SUSPICIOUS_STEW, Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.COOKED_MUTTON, Items.COOKED_CHICKEN, Items.COOKED_RABBIT, Items.COOKED_COD, Items.COOKED_SALMON, Items.DRIED_KELP, Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.HONEY_BOTTLE);
        }
    }

    public static ItemStack generateItemForMerchant(Random rand, int merchantType, int level, int rarity) {
        Item item = pickItemForMerchant(rand, merchantType);
        ItemStack stack = new ItemStack(item);
        CompoundNBT nbt = stack.getOrCreateTag();
        nbt.putInt("ItemLevel", level);
        int r = Math.min(rarity, Rarity.values().length - 1);
        nbt.putInt("RarityLevel", r);

        // Если это НЕ еда и НЕ кристаллы, выдаем статы
        if (!item.isEdible() && item != ModItems.SMALL_MAGIC_CRYSTAL.get()
                && item != ModItems.MEDIUM_MAGIC_CRYSTAL.get() && item != ModItems.LARGE_MAGIC_CRYSTAL.get()) {
            int totalStats = Math.max(1, (int) (level * Rarity.values()[r].getMultiplier()));
            int str = 0, dex = 0, intel = 0, luck = 0, vit = 0;
            for (int i = 0; i < totalStats; i++) {
                switch (rand.nextInt(5)) {
                    case 0: str++; break;
                    case 1: dex++; break;
                    case 2: intel++; break;
                    case 3: luck++; break;
                    case 4: vit++; break;
                }
            }
            nbt.putInt("StatStrength", str);
            nbt.putInt("StatDexterity", dex);
            nbt.putInt("StatIntelligence", intel);
            nbt.putInt("StatLuck", luck);
            nbt.putInt("StatVitality", vit);
        }
        return stack;
    }

    public static int getTotalCopper(int copper, int silver, int gold) {
        return copper + silver * 100 + gold * 10000;
    }

    public static void setFromTotalCopper(com.example.examplemod.capability.IPlayerLevel data, int total) {
        data.setGold(total / 10000);
        data.setSilver((total % 10000) / 100);
        data.setCopper(total % 100);
    }
}