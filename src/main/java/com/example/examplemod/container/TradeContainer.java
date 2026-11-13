package com.example.examplemod.container;

import com.example.examplemod.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.List;

public class TradeContainer extends Container {

    private final Inventory sellInventory = new Inventory(10);
    public final List<ItemStack> items = new ArrayList<>();
    public final List<Integer> prices = new ArrayList<>();
    public final int copper, silver, gold;

    public TradeContainer(int id, PlayerInventory playerInventory, PacketBuffer data) {
        this(id, playerInventory, readItems(data), readPrices(data), data.readInt(), data.readInt(), data.readInt());
    }

    public TradeContainer(int id, PlayerInventory playerInventory, List<ItemStack> items, List<Integer> prices, int copper, int silver, int gold) {
        super(ModContainers.TRADE_CONTAINER.get(), id);
        this.items.addAll(items);
        this.prices.addAll(prices);
        this.copper = copper;
        this.silver = silver;
        this.gold = gold;

        // 10 слотов для продажи (индексы 0-9)
        for (int i = 0; i < 10; i++) {
            this.addSlot(new Slot(sellInventory, i, 170 + i * 18, 140) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (stack.getItem() == ModItems.STATS_BOOK.get()) return false;

                    if (stack.hasTag()) {
                        CompoundNBT nbt = stack.getTag();
                        // НОВОЕ: разрешаем тома, реликвии, а также обычные предметы с редкостью/уровнем
                        return nbt.contains("ItemLevel")
                                || nbt.contains("RarityLevel")
                                || nbt.contains("SpellId")
                                || nbt.contains("RelicBasePrice");
                    }
                    return false;
                }
            });
        }

        // Инвентарь игрока (индексы 10-36)
        for (int r = 0; r < 3; ++r) {
            for (int c = 0; c < 9; ++c) {
                this.addSlot(new Slot(playerInventory, c + r * 9 + 9, 179 + c * 18, 180 + r * 18));
            }
        }

        // Хотбар (индексы 37-45)
        for (int c = 0; c < 9; ++c) {
            this.addSlot(new Slot(playerInventory, c, 179 + c * 18, 238));
        }
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 10) {
                if (!this.moveItemStackTo(itemstack1, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, 10, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

    private static List<ItemStack> readItems(PacketBuffer data) {
        int size = data.readInt();
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < size; i++) list.add(data.readItem());
        return list;
    }

    private static List<Integer> readPrices(PacketBuffer data) {
        int size = data.readInt();
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < size; i++) list.add(data.readInt());
        return list;
    }

    @Override
    public void removed(PlayerEntity playerIn) {
        super.removed(playerIn);
        this.clearContainer(playerIn, playerIn.level, sellInventory);
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return true;
    }
}