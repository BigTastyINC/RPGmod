package com.example.examplemod.item;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.nbt.CompoundNBT;

import java.util.Random;

public class ItemStatHelper {

    public static boolean isWeapon(ItemStack stack) {
        return stack.getItem() instanceof SwordItem ||
                stack.getItem() instanceof AxeItem ||
                stack.getItem() instanceof TridentItem;
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
        boolean isArmorPiece = isArmor(stack);

        for (int i = 0; i < totalStatPoints; i++) {
            if (isArmorPiece) {
                // Броня: 6 вариантов, Жизнь выпадает чаще (2 шанса из 6)
                int statRoll = rand.nextInt(6);
                switch (statRoll) {
                    case 0: str++; break;
                    case 1: dex++; break;
                    case 2: intel++; break;
                    case 3: luck++; break;
                    case 4:
                    case 5: vit++; break;
                }
            } else {
                // Оружие: 4 варианта, БЕЗ Жизни
                int statRoll = rand.nextInt(4);
                switch (statRoll) {
                    case 0: str++; break;
                    case 1: dex++; break;
                    case 2: intel++; break;
                    case 3: luck++; break;
                }
            }
        }

        // Гарантируем, что оружие получит хотя бы Силу и Ловкость
        if (str == 0 && !isArmorPiece) str = 1;
        if (dex == 0 && !isArmorPiece) dex = 1;
        if (vit == 0 && isArmorPiece) vit = 1;

        nbt.putInt("StatStrength", str);
        nbt.putInt("StatDexterity", dex);
        nbt.putInt("StatIntelligence", intel);
        nbt.putInt("StatLuck", luck);
        nbt.putInt("StatVitality", vit);
    }
}