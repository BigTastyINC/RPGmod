package com.example.examplemod.jewelry;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public interface IJewelryInventory {
    // Всего 7 слотов: 0-1 кольца, 2 амулет, 3-4 серьги, 5-6 браслеты
    int TOTAL_SLOTS = 7;

    ItemStack getSlot(int index);
    void setSlot(int index, ItemStack stack);
    boolean isSlotValid(int index, ItemStack stack);

    // Удобные геттеры по типу
    int getFirstSlotOfType(JewelryType type);
    boolean hasEquipped(JewelryType type);
    int countEquipped(JewelryType type);

    // Проверка типа слота
    JewelryType getSlotType(int index);

    CompoundNBT serializeNBT();
    void deserializeNBT(CompoundNBT nbt);
}