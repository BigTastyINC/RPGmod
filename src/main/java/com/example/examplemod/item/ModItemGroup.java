package com.example.examplemod.item;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModItemGroup extends ItemGroup {
    public static final ModItemGroup INSTANCE = new ModItemGroup("examplemod", () -> new ItemStack(ModItems.MITHRIL_SWORD.get()));

    private final java.util.function.Supplier<ItemStack> icon;

    public ModItemGroup(String label, java.util.function.Supplier<ItemStack> icon) {
        super(label);
        this.icon = icon;
    }

    @Override
    public ItemStack makeIcon() {
        return icon.get();
    }
}