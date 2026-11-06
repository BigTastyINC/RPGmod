package com.example.examplemod.compat;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class TinkersCompat {

    /**
     * Проверяет, является ли предмет оружием из Tinkers' Construct.
     * Мы смотрим на NBT-теги, которые мод присваивает своим инструментам.
     */
    public static boolean isTinkersWeapon(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return false;
        }

        CompoundNBT nbt = stack.getTag();
        if (nbt == null) {
            return false;
        }

        // 1. Проверяем, что это вообще инструмент из Tinkers' Construct
        //    У таких предметов есть тег "tic_materials"
        if (!nbt.contains("tic_materials")) {
            return false;
        }

        // 2. Проверяем, что это именно оружие ближнего боя.
        //    Tinkers' Construct использует тег "tconstruct:modifiable/melee/weapon" для мечей и топоров.
        //    Если тег есть, значит это оружие. Кирки и лопаты этот тег не получают.
        return stack.getItem().isIn(net.minecraft.tags.ItemTags.makeWrapperTag("tconstruct:modifiable/melee/weapon"));
    }
}