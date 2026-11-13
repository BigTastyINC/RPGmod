package com.example.examplemod.jewelry;

import com.example.examplemod.item.ItemAffix;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JewelryItem extends Item implements ICurioItem {
    private final JewelryType type;

    public JewelryItem(Properties properties, JewelryType type) {
        super(properties);
        this.type = type;
    }

    public JewelryType getJewelryType() {
        return type;
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        return new StringTextComponent(type.getDisplayName()).withStyle(TextFormatting.GOLD);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    public void generateJewelryStats(ItemStack stack, int playerLevel, com.example.examplemod.item.Rarity rarity) {
        CompoundNBT nbt = stack.getOrCreateTag();
        nbt.putInt("ItemLevel", playerLevel);
        nbt.putInt("RarityLevel", rarity.ordinal());
        nbt.putString("JewelryType", type.getId());

        java.util.Random rand = new java.util.Random();
        int totalStatPoints = Math.max(1, (int) (((playerLevel / 15.0) + 1) * rarity.getMultiplier()));

        int str = 0, dex = 0, intel = 0, luck = 0, vit = 0;
        for (int i = 0; i < totalStatPoints; i++) {
            switch (rand.nextInt(5)) {
                case 0: str++; break;
                case 1: dex++; break;
                case 2: intel++; break;
                case 3: luck++; break;
                case 4: vit++; break;
            }
        }

        nbt.putInt("StatStrength", str);
        nbt.putInt("StatDexterity", dex);
        nbt.putInt("StatIntelligence", intel);
        nbt.putInt("StatLuck", luck);
        nbt.putInt("StatVitality", vit);

        int count;
        switch (rarity) {
            case COMMON: count = 0; break;
            case UNCOMMON: count = 1; break;
            case RARE: count = 2; break;
            case EPIC: count = 3; break;
            case LEGENDARY: count = 4; break;
            default: count = 0;
        }

        List<ItemAffix> pool = new ArrayList<>();
        for (ItemAffix a : ItemAffix.values()) {
            if (a.getType() == ItemAffix.Type.JEWELRY) pool.add(a);
        }
        Collections.shuffle(pool);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(count, pool.size()); i++) {
            if (sb.length() > 0) sb.append(",");
            sb.append(pool.get(i).getId());
        }
        if (sb.length() > 0) nbt.putString("Affixes", sb.toString());
        else nbt.remove("Affixes");
    }
}