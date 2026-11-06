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

        // 1. Стандартные ванильные классы
        if (item instanceof SwordItem) return true;
        if (item instanceof AxeItem) return true;
        if (item instanceof TridentItem) return true;

        // 2. Tinkers' Construct (безопасная проверка, только если мод загружен)
        if (ModList.get().isLoaded("tconstruct")) {
            if (TinkersCompat.isTinkersWeapon(stack)) return true;
        }

        // 3. Явно исключаем инструменты
        if (item instanceof PickaxeItem) return false;
        if (item instanceof ShovelItem) return false;
        if (item instanceof HoeItem) return false;

        return false;
    }

    public static boolean isArmor(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem;
    }

    public static boolean isGear(ItemStack stack) {
        return isWeapon(stack) || isArmor(stack);
    }

    public static void generateStats(ItemStack stack, int playerLevel) {
        if (stack.isEmpty() || !isGear(stack)) return;

        Random rand = new Random();
        int roll = rand.nextInt(100);
        Rarity rarity;
        if (roll < 50) rarity = Rarity.COMMON;
        else if (roll < 80) rarity = Rarity.UNCOMMON;
        else if (roll < 95) rarity = Rarity.RARE;
        else if (roll < 99) rarity = Rarity.EPIC;
        else rarity = Rarity.LEGENDARY;

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
}