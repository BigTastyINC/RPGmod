package com.example.examplemod.item;

import com.example.examplemod.compat.TinkersCompat;
import com.example.examplemod.jewelry.JewelryItem;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ItemStatHelper {

    public static boolean isWeapon(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof SwordItem) return true;
        if (item instanceof AxeItem) return true;
        if (item instanceof TridentItem) return true;
        if (ModList.get().isLoaded("tconstruct")) {
            if (TinkersCompat.isTinkersWeapon(stack)) return true;
        }
        if (item instanceof PickaxeItem) return false;
        if (item instanceof ShovelItem) return false;
        if (item instanceof HoeItem) return false;
        return false;
    }

    public static boolean isArmor(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof ArmorItem) return true;
        return stack.getAttributeModifiers(EquipmentSlotType.CHEST).containsKey(Attributes.ARMOR)
                || stack.getAttributeModifiers(EquipmentSlotType.HEAD).containsKey(Attributes.ARMOR)
                || stack.getAttributeModifiers(EquipmentSlotType.LEGS).containsKey(Attributes.ARMOR)
                || stack.getAttributeModifiers(EquipmentSlotType.FEET).containsKey(Attributes.ARMOR);
    }

    public static boolean isStaff(ItemStack stack) {
        return stack.getItem() instanceof StaffItem;
    }

    public static boolean isJewelry(ItemStack stack) {
        return stack.getItem() instanceof JewelryItem;
    }

    public static boolean isGear(ItemStack stack) {
        return isWeapon(stack) || isArmor(stack) || isStaff(stack) || isJewelry(stack);
    }

    public static boolean isPotion(ItemStack stack) {
        return stack.getItem() instanceof RpgPotionItem;
    }

    public static Rarity rollRandomRarity() {
        int roll = new Random().nextInt(100);
        if (roll < 50) return Rarity.COMMON;
        if (roll < 80) return Rarity.UNCOMMON;
        if (roll < 95) return Rarity.RARE;
        if (roll < 99) return Rarity.EPIC;
        return Rarity.LEGENDARY;
    }

    public static void generateStats(ItemStack stack, int playerLevel) {
        generateStatsWithRarity(stack, playerLevel, rollRandomRarity());
    }

    public static void generateStatsWithRarity(ItemStack stack, int playerLevel, Rarity rarity) {
        if (stack.isEmpty()) return;

        // Бижутерия — отдельный путь
        if (isJewelry(stack)) {
            ((JewelryItem) stack.getItem()).generateJewelryStats(stack, playerLevel, rarity);
            return;
        }

        if (!isGear(stack)) return;

        Random rand = new Random();
        CompoundNBT nbt = stack.getOrCreateTag();
        nbt.putInt("ItemLevel", playerLevel);
        nbt.putInt("RarityLevel", rarity.ordinal());

        int totalStatPoints = Math.max(1, (int) (((playerLevel / 10.0) + 1) * rarity.getMultiplier()));
        int str = 0, dex = 0, intel = 0, luck = 0, vit = 0;

        if (isArmor(stack)) {
            for (int i = 0; i < totalStatPoints; i++) {
                int r = rand.nextInt(6);
                switch (r) {
                    case 0: str++; break;
                    case 1: dex++; break;
                    case 2: intel++; break;
                    case 3: luck++; break;
                    case 4:
                    case 5: vit++; break;
                }
            }
            if (vit == 0 && totalStatPoints > 0) vit = 1;
        } else if (isStaff(stack)) {
            for (int i = 0; i < totalStatPoints; i++) {
                if (rand.nextBoolean()) intel++;
                else luck++;
            }
            if (intel == 0 && totalStatPoints > 0) intel = 1;
            if (luck == 0 && totalStatPoints > 0) luck = 1;
        } else {
            for (int i = 0; i < totalStatPoints; i++) {
                if (rand.nextBoolean()) str++;
                else dex++;
            }
            if (str == 0 && totalStatPoints > 0) str = 1;
            if (dex == 0 && totalStatPoints > 0) dex = 1;
        }

        nbt.putInt("StatStrength", str);
        nbt.putInt("StatDexterity", dex);
        nbt.putInt("StatIntelligence", intel);
        nbt.putInt("StatLuck", luck);
        nbt.putInt("StatVitality", vit);

        rollAffixes(stack, rarity);
        applyMaterialAffix(stack);
    }

    public static void applyPotionRarity(ItemStack stack, int playerLevel, Rarity rarity) {
        CompoundNBT nbt = stack.getOrCreateTag();
        nbt.putInt("ItemLevel", playerLevel);
        nbt.putInt("RarityLevel", rarity.ordinal());
    }

    public static void rollAffixes(ItemStack stack, Rarity rarity) {
        CompoundNBT nbt = stack.getOrCreateTag();
        ItemAffix.Type type;
        if (isStaff(stack)) type = ItemAffix.Type.STAFF;
        else if (isArmor(stack)) type = ItemAffix.Type.ARMOR;
        else if (isWeapon(stack)) type = ItemAffix.Type.WEAPON;
        else if (isJewelry(stack)) type = ItemAffix.Type.JEWELRY;
        else return;

        int count;
        switch (rarity) {
            case COMMON: count = 0; break;
            case UNCOMMON: count = 1; break;
            case RARE: count = 2; break;
            case EPIC: count = 3; break;
            case LEGENDARY: count = 4; break;
            default: count = 0;
        }

        List<ItemAffix> pool = new ArrayList<>();
        for (ItemAffix a : ItemAffix.values()) {
            if (a.getType() == type) pool.add(a);
        }
        Collections.shuffle(pool);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(count, pool.size()); i++) {
            if (sb.length() > 0) sb.append(",");
            sb.append(pool.get(i).getId());
        }
        if (sb.length() > 0) nbt.putString("Affixes", sb.toString());
        else nbt.remove("Affixes");
    }

    public static List<ItemAffix> getAffixes(ItemStack stack) {
        List<ItemAffix> list = new ArrayList<>();
        CompoundNBT nbt = stack.getTag();
        if (nbt == null || !nbt.contains("Affixes")) return list;
        String str = nbt.getString("Affixes");
        for (String id : str.split(",")) {
            ItemAffix a = ItemAffix.byId(id);
            if (a != null) list.add(a);
        }
        return list;
    }

    public static boolean hasAffix(ItemStack stack, ItemAffix affix) {
        return getAffixes(stack).contains(affix);
    }

    public static void applyMaterialAffix(ItemStack stack) {
        if (stack.getItem().getRegistryName() == null) return;
        String name = stack.getItem().getRegistryName().getPath();
        CompoundNBT nbt = stack.getOrCreateTag();
        if (name.contains("silver")) nbt.putBoolean("silver_bonus", true);
        if (name.contains("orichalcum")) nbt.putBoolean("orichalcum_bonus", true);
    }

    public static boolean hasSilverBonus(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("silver_bonus");
    }

    public static boolean hasOrichalcumBonus(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("orichalcum_bonus");
    }
}