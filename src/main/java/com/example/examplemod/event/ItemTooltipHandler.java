package com.example.examplemod.event;

import com.example.examplemod.item.Rarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "examplemod")
public class ItemTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        CompoundNBT nbt = stack.getTag();

        if (nbt != null && nbt.contains("ItemLevel")) {
            int level = nbt.getInt("ItemLevel");
            Rarity rarity = Rarity.values()[nbt.getInt("RarityLevel")];

            event.getToolTip().add(new StringTextComponent(rarity.getColor() + "[" + rarity.getName() + "] Уровень: " + level));
            event.getToolTip().add(new StringTextComponent("§8--------------------"));

            if (nbt.getInt("StatStrength") > 0) event.getToolTip().add(new StringTextComponent("§c+" + nbt.getInt("StatStrength") + " Сила"));
            if (nbt.getInt("StatDexterity") > 0) event.getToolTip().add(new StringTextComponent("§b+" + nbt.getInt("StatDexterity") + " Ловкость"));
            if (nbt.getInt("StatIntelligence") > 0) event.getToolTip().add(new StringTextComponent("§d+" + nbt.getInt("StatIntelligence") + " Интеллект"));
            if (nbt.getInt("StatLuck") > 0) event.getToolTip().add(new StringTextComponent("§e+" + nbt.getInt("StatLuck") + " Удача"));
            if (nbt.getInt("StatVitality") > 0) event.getToolTip().add(new StringTextComponent("§4+" + nbt.getInt("StatVitality") + " Жизнь"));
        }
    }
}