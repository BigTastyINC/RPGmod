package com.example.examplemod.item;

import com.example.examplemod.compat.TinkersCompat;
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
        return stack.getItem() instanceof ArmorItem;
    }

    public static boolean isStaff(ItemStack stack) {
        return stack.getItem() instanceof StaffItem;
    }

    public static boolean isGear(ItemStack stack) {
        return isWeapon(stack) || isArmor(stack) || isStaff(stack);
    }

    // НОВОЕ: Проверяем, является ли предмет нашим зельем
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
        if (stack.isEmpty() || !isGear(stack)) return;

        Random rand = new Random();
        CompoundNBT nbt = stack.getOrCreateTag();
        nbt.putInt("ItemLevel", playerLevel);
        nbt.putInt("RarityLevel", rarity.ordinal());

        int totalStatPoints = (int) (playerLevel * rarity.getMultiplier());
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
            if (vit == 0) vit = 1;
        } else if (isStaff(stack)) {
            // Посохи дают Интеллект и Удачу
            for (int i = 0; i < totalStatPoints; i++) {
                if (rand.nextBoolean()) intel++;
                else luck++;
            }
            if (intel == 0) intel = 1;
            if (luck == 0) luck = 1;
        } else {
            for (int i = 0; i < totalStatPoints; i++) {
                if (rand.nextBoolean()) str++;
                else dex++;
            }
            if (str == 0) str = 1;
            if (dex == 0) dex = 1;
        }

        nbt.putInt("StatStrength", str);
        nbt.putInt("StatDexterity", dex);
        nbt.putInt("StatIntelligence", intel);
        nbt.putInt("StatLuck", luck);
        nbt.putInt("StatVitality", vit);
    }

    // НОВОЕ: Выдаем зелью уровень и редкость
    public static void applyPotionRarity(ItemStack stack, int playerLevel, Rarity rarity) {
        CompoundNBT nbt = stack.getOrCreateTag();
        nbt.putInt("ItemLevel", playerLevel);
        nbt.putInt("RarityLevel", rarity.ordinal());
    }
}