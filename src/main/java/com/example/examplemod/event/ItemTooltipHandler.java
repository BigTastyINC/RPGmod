package com.example.examplemod.event;

import com.example.examplemod.buffs.RpgBuffs;
import com.example.examplemod.item.Rarity;
import com.example.examplemod.item.RpgPotionItem;
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

        if (nbt == null) return;

        // === Оружие / броня / зелья ===
        if (nbt.contains("ItemLevel")) {
            int level = nbt.getInt("ItemLevel");
            int rIndex = Math.min(4, nbt.getInt("RarityLevel"));
            Rarity rarity = Rarity.values()[rIndex];

            event.getToolTip().add(new StringTextComponent(rarity.getColor() + "[" + rarity.getName() + "] Уровень: " + level));
            event.getToolTip().add(new StringTextComponent("§8--------------------"));

            if (stack.getItem() instanceof RpgPotionItem) {
                RpgPotionItem potion = (RpgPotionItem) stack.getItem();
                int amount = (int) (level * 5 * rarity.getMultiplier());
                if (potion.isManaPotion()) event.getToolTip().add(new StringTextComponent("§bВосстанавливает " + amount + " маны"));
                else event.getToolTip().add(new StringTextComponent("§cВосстанавливает " + amount + " здоровья"));
            } else {
                if (nbt.getInt("StatStrength") > 0) event.getToolTip().add(new StringTextComponent("§c+" + nbt.getInt("StatStrength") + " Сила"));
                if (nbt.getInt("StatDexterity") > 0) event.getToolTip().add(new StringTextComponent("§b+" + nbt.getInt("StatDexterity") + " Ловкость"));
                if (nbt.getInt("StatIntelligence") > 0) event.getToolTip().add(new StringTextComponent("§d+" + nbt.getInt("StatIntelligence") + " Интеллект"));
                if (nbt.getInt("StatLuck") > 0) event.getToolTip().add(new StringTextComponent("§e+" + nbt.getInt("StatLuck") + " Удача"));
                if (nbt.getInt("StatVitality") > 0) event.getToolTip().add(new StringTextComponent("§4+" + nbt.getInt("StatVitality") + " Жизнь"));
            }
        }
        // === Магическая еда: есть RarityLevel, но нет ItemLevel ===
        else if (nbt.contains("RarityLevel")) {
            int rIndex = Math.min(4, nbt.getInt("RarityLevel"));
            Rarity rarity = Rarity.values()[rIndex];
            int buffId = RpgBuffs.getBuffForFood(stack.getItem());

            // Считаем финальную силу баффа и форматируем описание
            float power = RpgBuffs.BASE_POWER[buffId] * RpgBuffs.RARITY_MULTIPLIER[rIndex];
            String desc = RpgBuffs.DESCRIPTIONS[buffId];

            // Подставляем фактическое значение в описание
            if (desc.contains("%")) {
                int percent = (int) Math.round(power * 100);
                desc = desc.replace("%", "" + percent);
            } else if (desc.contains("HP") || desc.contains("мана")) {
                desc = desc.replace("X", String.format("%.1f", power));
            }

            event.getToolTip().add(new StringTextComponent(""));
            event.getToolTip().add(new StringTextComponent(rarity.getColor() + "[" + rarity.getName() + "]"));
            event.getToolTip().add(new StringTextComponent("§b" + RpgBuffs.NAMES[buffId] + " §7— " + desc));
            event.getToolTip().add(new StringTextComponent("§7Длительность: " + RpgBuffs.DURATION_MINUTES[rIndex] + " мин"));
        }
    }
}