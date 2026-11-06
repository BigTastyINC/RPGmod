package com.example.examplemod.compat;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class TinkersCompat {

    public static boolean isTinkersWeapon(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return false;
        CompoundNBT nbt = stack.getTag();
        if (nbt == null) return false;

        // У инструментов Tinkers' Construct всегда есть тег "tic_materials"
        if (!nbt.contains("tic_materials")) return false;

        // Проверяем имя предмета — если содержит "sword", "cleaver", "rapier", "longsword" и т.п.
        // Это надёжнее, чем теги, и не ломает сборку.
        String name = stack.getItem().getRegistryName() != null
                ? stack.getItem().getRegistryName().getPath()
                : "";
        return name.contains("sword") || name.contains("cleaver") || name.contains("rapier")
                || name.contains("katana") || name.contains("longsword") || name.contains("broadsword");
    }
}