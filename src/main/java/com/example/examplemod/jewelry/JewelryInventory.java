package com.example.examplemod.jewelry;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class JewelryInventory implements IJewelryInventory {
    private final ItemStack[] slots = new ItemStack[TOTAL_SLOTS];

    public JewelryInventory() {
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            slots[i] = ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack getSlot(int index) {
        if (index < 0 || index >= TOTAL_SLOTS) return ItemStack.EMPTY;
        return slots[index];
    }

    @Override
    public void setSlot(int index, ItemStack stack) {
        if (index < 0 || index >= TOTAL_SLOTS) return;
        slots[index] = stack == null ? ItemStack.EMPTY : stack;
    }

    @Override
    public boolean isSlotValid(int index, ItemStack stack) {
        JewelryType slotType = getSlotType(index);
        if (slotType == null) return false;
        if (stack.isEmpty()) return true;
        if (!(stack.getItem() instanceof JewelryItem)) return false;
        JewelryItem item = (JewelryItem) stack.getItem();
        return item.getJewelryType() == slotType;
    }

    @Override
    public JewelryType getSlotType(int index) {
        switch (index) {
            case 0:
            case 1: return JewelryType.RING;
            case 2: return JewelryType.AMULET;
            case 3:
            case 4: return JewelryType.EARRING;
            case 5:
            case 6: return JewelryType.BRACELET;
            default: return null;
        }
    }

    @Override
    public int getFirstSlotOfType(JewelryType type) {
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (getSlotType(i) == type) return i;
        }
        return -1;
    }

    @Override
    public boolean hasEquipped(JewelryType type) {
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (getSlotType(i) == type && !slots[i].isEmpty()) return true;
        }
        return false;
    }

    @Override
    public int countEquipped(JewelryType type) {
        int count = 0;
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (getSlotType(i) == type && !slots[i].isEmpty()) count++;
        }
        return count;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (!slots[i].isEmpty()) {
                CompoundNBT slotNbt = new CompoundNBT();
                slots[i].save(slotNbt);
                nbt.put("slot_" + i, slotNbt);
            }
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (nbt.contains("slot_" + i)) {
                slots[i] = ItemStack.of(nbt.getCompound("slot_" + i));
            } else {
                slots[i] = ItemStack.EMPTY;
            }
        }
    }
}