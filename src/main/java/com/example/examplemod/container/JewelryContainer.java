package com.example.examplemod.container;

import com.example.examplemod.jewelry.IJewelryInventory;
import com.example.examplemod.jewelry.JewelryInventory;
import com.example.examplemod.jewelry.JewelryType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class JewelryContainer extends Container {

    private final IJewelryInventory jewelryInventory;

    // Серверный конструктор (с capability)
    public JewelryContainer(int id, PlayerInventory playerInventory, IJewelryInventory jewelry) {
        super(ModContainers.JEWELRY_CONTAINER.get(), id);
        this.jewelryInventory = jewelry;

        // 7 слотов бижутерии (слева, сеткой)
        int[][] positions = {
                {26, 20},  // 0 — кольцо 1
                {26, 50},  // 1 — кольцо 2
                {60, 35},  // 2 — амулет
                {94, 20},  // 3 — серьга 1
                {94, 50},  // 4 — серьга 2
                {128, 20}, // 5 — браслет 1
                {128, 50}  // 6 — браслет 2
        };

        for (int i = 0; i < IJewelryInventory.TOTAL_SLOTS; i++) {
            final int index = i;
            this.addSlot(new Slot(new net.minecraft.inventory.Inventory(1) {
                @Override public ItemStack getItem(int s) { return jewelryInventory.getSlot(index); }
                @Override public void setItem(int s, ItemStack stack) { jewelryInventory.setSlot(index, stack); }
                @Override public ItemStack removeItem(int s, int amount) {
                    ItemStack stack = jewelryInventory.getSlot(index);
                    if (stack.isEmpty()) return ItemStack.EMPTY;
                    ItemStack split = stack.split(amount);
                    if (!split.isEmpty()) jewelryInventory.setSlot(index, stack);
                    return split;
                }
            }, 0, positions[i][0], positions[i][1]) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return jewelryInventory.isSlotValid(index, stack);
                }
            });
        }

        // Инвентарь игрока (3x9)
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                this.addSlot(new Slot(playerInventory, c + r * 9 + 9, 8 + c * 18, 84 + r * 18));
            }
        }

        // Хотбар
        for (int c = 0; c < 9; c++) {
            this.addSlot(new Slot(playerInventory, c, 8 + c * 18, 142));
        }
    }

    // Клиентский конструктор (читает из PacketBuffer)
    public JewelryContainer(int id, PlayerInventory playerInventory, PacketBuffer data) {
        this(id, playerInventory, readJewelry(data));
    }

    private static IJewelryInventory readJewelry(PacketBuffer data) {
        JewelryInventory inv = new JewelryInventory();
        int size = data.readInt();
        for (int i = 0; i < size; i++) {
            inv.setSlot(i, data.readItem());
        }
        return inv;
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();

            if (index < IJewelryInventory.TOTAL_SLOTS) {
                // Из бижутерии → в инвентарь
                if (!this.moveItemStackTo(stack, IJewelryInventory.TOTAL_SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                // Из инвентаря → в бижутерию (если подходит)
                if (!this.moveItemStackTo(stack, 0, IJewelryInventory.TOTAL_SLOTS, false)) return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return true;
    }
}