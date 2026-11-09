package com.example.examplemod.shop;

import com.example.examplemod.item.ItemStatHelper;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.item.Rarity;
import com.example.examplemod.item.SpellTomeItem;
import com.example.examplemod.spells.Spell;
import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShopUtils {

    public static String formatPrice(int copper) {
        if (copper < 100) return copper + "м";
        int s = copper / 100;
        int m = copper % 100;
        if (s < 100) return m > 0 ? s + "с " + m + "м" : s + "с";
        int g = s / 100;
        s = s % 100;
        String res = g + "з";
        if (s > 0) res += " " + s + "с";
        if (m > 0) res += " " + m + "м";
        return res;
    }

    public static int calcSellPrice(ItemStack stack) {
        if (stack.getItem() == ModItems.LUCK_POTION.get()) return 100;
        if (stack.getItem() instanceof SpellTomeItem) return 500;

        CompoundNBT nbt = stack.getTag();
        if (nbt == null || !nbt.contains("ItemLevel")) return 2;

        // НОВОЕ: Уникальная цена реликвии
        if (nbt.contains("RelicBasePrice")) {
            int basePrice = nbt.getInt("RelicBasePrice");
            int rarity = Math.min(4, nbt.getInt("RarityLevel"));
            float mult = (float) Rarity.values()[rarity].getMultiplier();
            return (int) (basePrice * mult);
        }

        int level = nbt.getInt("ItemLevel");
        int rarity = Math.min(4, nbt.getInt("RarityLevel"));
        int stats = nbt.getInt("StatStrength") + nbt.getInt("StatDexterity")
                + nbt.getInt("StatIntelligence") + nbt.getInt("StatLuck")
                + nbt.getInt("StatVitality");
        float matMult = getMaterialMultiplier(stack);

        if (stack.getItem().isEdible()) return (level * 5 + rarity * 50) * 2;
        if (stack.getItem() instanceof com.example.examplemod.item.RpgPotionItem) return (level * 5 + rarity * 20) * 2;

        if (stack.getItem() == ModItems.SMALL_MAGIC_CRYSTAL.get()) return (level * 2 + rarity * 10);
        if (stack.getItem() == ModItems.MEDIUM_MAGIC_CRYSTAL.get()) return (level * 5 + rarity * 25);
        if (stack.getItem() == ModItems.LARGE_MAGIC_CRYSTAL.get()) return (level * 10 + rarity * 60);

        if (stack.getItem() instanceof com.example.examplemod.item.StaffItem) {
            int energy = nbt.contains("StaffEnergy") ? nbt.getInt("StaffEnergy") : 500;
            int base = (level * level * 3 / 7) + rarity * 20 + stats * 5;
            float basePrice = base * matMult;
            float energyBonus = (energy / 1000.0f) * 1500f;
            return (int) (basePrice + energyBonus);
        }

        int base = (level * level * 3 / 7) + rarity * 20 + stats * 5;
        return (int) (base * matMult);
    }

    public static int calcBuyPrice(ItemStack stack) {
        if (stack.getItem() == ModItems.LUCK_POTION.get()) return 30000;

        if (stack.getItem() instanceof SpellTomeItem) {
            CompoundNBT nbt = stack.getOrCreateTag();
            int spellLevel = nbt.getInt("SpellLevel");
            int price = 10000;
            for (int i = 0; i < spellLevel; i++) price *= 10;
            return price;
        }

        CompoundNBT nbt = stack.getOrCreateTag();
        int level = Math.max(1, nbt.getInt("ItemLevel"));
        int rarity = Math.min(4, nbt.getInt("RarityLevel"));

        // НОВОЕ: Уникальная цена реликвии (покупка в 3 раза дороже продажи)
        if (nbt.contains("RelicBasePrice")) {
            int basePrice = nbt.getInt("RelicBasePrice");
            float mult = (float) Rarity.values()[rarity].getMultiplier();
            return (int) (basePrice * mult * 3);
        }

        if (stack.getItem().isEdible()) return level * 30 + rarity * 300;
        if (stack.getItem() instanceof com.example.examplemod.item.RpgPotionItem) return level * 10 + rarity * 50;

        if (stack.getItem() == ModItems.SMALL_MAGIC_CRYSTAL.get()) return (level * 4 + rarity * 20);
        if (stack.getItem() == ModItems.MEDIUM_MAGIC_CRYSTAL.get()) return (level * 10 + rarity * 60);
        if (stack.getItem() == ModItems.LARGE_MAGIC_CRYSTAL.get()) return (level * 20 + rarity * 150);

        float matMult = getMaterialMultiplier(stack);

        if (stack.getItem() instanceof com.example.examplemod.item.StaffItem) {
            int energy = nbt.contains("StaffEnergy") ? nbt.getInt("StaffEnergy") : 500;
            int base = (level * level * 33 / 7) + rarity * 150;
            float basePrice = base * matMult;
            float energyBonus = (energy / 1000.0f) * 15000f;
            return (int) (basePrice + energyBonus);
        }

        int base = (level * level * 33 / 7) + rarity * 150;
        return (int) (base * matMult);
    }

    public static float getMaterialMultiplier(ItemStack stack) {
        Item item = stack.getItem();

        if (ItemStatHelper.isArmor(stack)) {
            ArmorItem armor = (ArmorItem) item;
            EquipmentSlotType slot = armor.getSlot();
            if (slot == EquipmentSlotType.MAINHAND || slot == EquipmentSlotType.OFFHAND) return 1.0f;
            double power = getArmorPower(stack, slot);
            double diamondPower = getDiamondArmorPower(slot);
            if (diamondPower <= 0) return 1.0f;
            return (float) (power / diamondPower);
        }

        if (ItemStatHelper.isWeapon(stack)) {
            double power = getWeaponPower(stack);
            if (power <= 0) return 1.0f;
            return (float) (power / 6.0);
        }

        if (item == ModItems.SMALL_STAFF.get()) return 2.0f;
        if (item == ModItems.MEDIUM_STAFF.get()) return 4.0f;
        if (item == ModItems.LARGE_STAFF.get()) return 8.0f;

        return 1.0f;
    }

    private static double getWeaponPower(ItemStack stack) {
        Multimap<Attribute, AttributeModifier> mods = stack.getAttributeModifiers(EquipmentSlotType.MAINHAND);
        double base = 0, mult = 1;
        for (AttributeModifier mod : mods.get(Attributes.ATTACK_DAMAGE)) {
            if (mod.getOperation() == AttributeModifier.Operation.ADDITION) base += mod.getAmount();
            else if (mod.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE) mult *= (1 + mod.getAmount());
            else if (mod.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) mult *= (1 + mod.getAmount());
        }
        return base * mult;
    }

    private static double getArmorPower(ItemStack stack, EquipmentSlotType slot) {
        Multimap<Attribute, AttributeModifier> mods = stack.getAttributeModifiers(slot);
        double armor = 0, tough = 0;
        for (AttributeModifier mod : mods.get(Attributes.ARMOR)) {
            if (mod.getOperation() == AttributeModifier.Operation.ADDITION) armor += mod.getAmount();
        }
        for (AttributeModifier mod : mods.get(Attributes.ARMOR_TOUGHNESS)) {
            if (mod.getOperation() == AttributeModifier.Operation.ADDITION) tough += mod.getAmount();
        }
        return armor + tough * 2;
    }

    private static double getDiamondArmorPower(EquipmentSlotType slot) {
        switch (slot) {
            case HEAD:  return 7.0;
            case CHEST: return 12.0;
            case LEGS:  return 10.0;
            case FEET:  return 7.0;
            default:    return 12.0;
        }
    }

    private static Item pickFrom(Random rand, Item... items) {
        return items[rand.nextInt(items.length)];
    }

    private static Item pickItemForMerchant(Random rand, int merchantType) {
        if (merchantType == 2) {
            return pickFrom(rand, ModItems.MANA_POTION.get(), ModItems.HEALTH_POTION.get(),
                    ModItems.LUCK_POTION.get(),
                    ModItems.SMALL_MAGIC_CRYSTAL.get(), ModItems.MEDIUM_MAGIC_CRYSTAL.get(), ModItems.LARGE_MAGIC_CRYSTAL.get(),
                    ModItems.SPELL_TOME.get(),
                    ModItems.SMALL_STAFF.get(), ModItems.MEDIUM_STAFF.get(), ModItems.LARGE_STAFF.get());
        }

        if (merchantType == 3) {
            return pickFrom(rand,
                    Items.BREAD, Items.COOKIE, Items.BAKED_POTATO, Items.PUMPKIN_PIE,
                    Items.MUSHROOM_STEW, Items.BEETROOT_SOUP, Items.RABBIT_STEW, Items.SUSPICIOUS_STEW,
                    Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.COOKED_MUTTON, Items.COOKED_CHICKEN,
                    Items.COOKED_RABBIT, Items.COOKED_COD, Items.COOKED_SALMON, Items.DRIED_KELP,
                    Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.HONEY_BOTTLE);
        }

        List<Item> pool = new ArrayList<>();

        if (merchantType == 0) {
            for (Item item : ForgeRegistries.ITEMS) {
                if (item instanceof HorseArmorItem || item instanceof ElytraItem) continue;
                if (!(item instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem)) continue;
                ItemStack testStack = new ItemStack(item);
                if (testStack.getMaxDamage() <= 0) continue;
                if (getWeaponPower(testStack) <= 0) continue;
                pool.add(item);
            }
        } else {
            for (Item item : ForgeRegistries.ITEMS) {
                if (item instanceof HorseArmorItem || item instanceof ElytraItem) continue;
                if (!(item instanceof ArmorItem)) continue;
                ArmorItem armor = (ArmorItem) item;
                EquipmentSlotType slot = armor.getSlot();
                if (slot == EquipmentSlotType.MAINHAND || slot == EquipmentSlotType.OFFHAND) continue;
                ItemStack testStack = new ItemStack(item);
                if (testStack.getMaxDamage() <= 0) continue;
                if (getArmorPower(testStack, slot) <= 0) continue;
                pool.add(item);
            }
        }

        if (pool.isEmpty()) {
            return merchantType == 0 ? Items.DIAMOND_SWORD : Items.DIAMOND_CHESTPLATE;
        }
        return pool.get(rand.nextInt(pool.size()));
    }

    public static ItemStack generateItemForMerchant(Random rand, int merchantType, int level, int rarity) {
        Item item = pickItemForMerchant(rand, merchantType);
        ItemStack stack = new ItemStack(item);
        CompoundNBT nbt = stack.getOrCreateTag();
        nbt.putInt("ItemLevel", level);
        int r = Math.min(rarity, Rarity.values().length - 1);
        nbt.putInt("RarityLevel", r);

        if (item instanceof SpellTomeItem) {
            Spell[] spells = Spell.values();
            Spell spell = spells[rand.nextInt(spells.length)];
            nbt.putString("SpellId", spell.getId());
            nbt.putInt("SpellLevel", spell.getDefaultLevel());
        } else if (item instanceof com.example.examplemod.item.StaffItem) {
            int randomEnergy = 126 + rand.nextInt(21641 - 126 + 1);
            nbt.putInt("StaffEnergy", randomEnergy);
            stack.setDamageValue(0);

            int totalStats = Math.max(1, (int) (((level / 10.0) + 1) * Rarity.values()[r].getMultiplier()));
            int intel = 0, luck = 0;
            for (int i = 0; i < totalStats; i++) {
                if (rand.nextBoolean()) intel++;
                else luck++;
            }
            if (intel == 0 && totalStats > 0) intel = 1;
            if (luck == 0 && totalStats > 0) luck = 1;

            nbt.putInt("StatStrength", 0);
            nbt.putInt("StatDexterity", 0);
            nbt.putInt("StatIntelligence", intel);
            nbt.putInt("StatLuck", luck);
            nbt.putInt("StatVitality", 0);
        } else if (!item.isEdible() && item != ModItems.SMALL_MAGIC_CRYSTAL.get()
                && item != ModItems.MEDIUM_MAGIC_CRYSTAL.get() && item != ModItems.LARGE_MAGIC_CRYSTAL.get()
                && item != ModItems.LUCK_POTION.get()) {
            int totalStats = Math.max(1, (int) (((level / 10.0) + 1) * Rarity.values()[r].getMultiplier()));
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