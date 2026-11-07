package com.example.examplemod.event;

import com.example.examplemod.buffs.RpgBuffs;
import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.item.Rarity;
import com.example.examplemod.item.RpgPotionItem;
import com.example.examplemod.shop.ShopUtils;
import com.example.examplemod.spells.Spell;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "examplemod")
public class ItemTooltipHandler {

    // Красивый формат с нулями (0з 0с 80м)
    private static String formatFullPrice(int copper) {
        int g = copper / 10000;
        int s = (copper % 10000) / 100;
        int m = copper % 100;
        return g + "з " + s + "с " + m + "м";
    }

    // Хелпер для вывода цены
    private static void addPriceTooltip(ItemTooltipEvent event, ItemStack stack) {
        int unitPrice = ShopUtils.calcSellPrice(stack);
        int totalPrice = unitPrice * stack.getCount();

        event.getToolTip().add(new StringTextComponent("§8--------------------"));

        // ИЗМЕНЕНО: Убрали "продажи"
        event.getToolTip().add(new StringTextComponent("§6Цена предмета: §e" + formatFullPrice(unitPrice)));

        if (stack.getCount() > 1) {
            // ИЗМЕНЕНО: Убрали "продажи"
            event.getToolTip().add(new StringTextComponent("§6Цена стака: §e" + formatFullPrice(totalPrice)));
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        CompoundNBT nbt = stack.getTag();

        PlayerEntity player = event.getPlayer();
        if (player == null) return;

        // === ЗЕЛЬЕ УДАЧИ ===
        if (stack.getItem() == ModItems.LUCK_POTION.get()) {
            event.getToolTip().add(new StringTextComponent("§6[Легендарный]"));
            event.getToolTip().add(new StringTextComponent("§8--------------------"));
            event.getToolTip().add(new StringTextComponent("§bУдача в профессии §7— +50% опыта профессий"));
            event.getToolTip().add(new StringTextComponent("§7Длительность: 10 мин"));
            addPriceTooltip(event, stack);
            return;
        }

        if (nbt == null) return;

        // === ТОМ ОБУЧЕНИЯ ===
        if (stack.getItem() == ModItems.SPELL_TOME.get()) {
            String spellId = nbt.getString("SpellId");
            if (!spellId.isEmpty()) {
                Spell spell = Spell.byId(spellId);
                int spellLevel = spell.getDefaultLevel();

                final int[] mageRank = {0};
                player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> mageRank[0] = data.getMageRank());

                Rarity rarity = Rarity.values()[Math.min(4, mageRank[0])];

                int finalDamage = (int) ((spell.getBaseDamage() + (spellLevel * 5)) * rarity.getMultiplier());
                int finalMana = (int) (spell.getBaseManaCost() * (1.0 - (spellLevel * 0.05)));

                event.getToolTip().add(new StringTextComponent(rarity.getColor() + spell.getName() + " [" + rarity.getName() + "]"));
                event.getToolTip().add(new StringTextComponent("§7Уровень заклинания: " + spellLevel));
                event.getToolTip().add(new StringTextComponent("§8--------------------"));
                event.getToolTip().add(new StringTextComponent("§cУрон: " + finalDamage));
                event.getToolTip().add(new StringTextComponent("§9Мана: " + finalMana));
                event.getToolTip().add(new StringTextComponent("§7" + spell.getDescription()));

                if (mageRank[0] < spellLevel) {
                    event.getToolTip().add(new StringTextComponent(""));
                    event.getToolTip().add(new StringTextComponent("§4⚠ Требуется Ранг Мага: " + spellLevel));
                }

                addPriceTooltip(event, stack);
            }
            return;
        }

        // === ОРУЖИЕ / БРОНЯ / ЗЕЛЬЯ / КРИСТАЛЛЫ ===
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
            }
            else if (stack.getItem() == ModItems.SMALL_MAGIC_CRYSTAL.get() ||
                    stack.getItem() == ModItems.MEDIUM_MAGIC_CRYSTAL.get() ||
                    stack.getItem() == ModItems.LARGE_MAGIC_CRYSTAL.get()) {
                int energy = (int) (level * 10 * rarity.getMultiplier());
                event.getToolTip().add(new StringTextComponent("§dМагическая энергия: " + energy));
            }
            else {
                if (nbt.getInt("StatStrength") > 0) event.getToolTip().add(new StringTextComponent("§c+" + nbt.getInt("StatStrength") + " Сила"));
                if (nbt.getInt("StatDexterity") > 0) event.getToolTip().add(new StringTextComponent("§b+" + nbt.getInt("StatDexterity") + " Ловкость"));
                if (nbt.getInt("StatIntelligence") > 0) event.getToolTip().add(new StringTextComponent("§d+" + nbt.getInt("StatIntelligence") + " Интеллект"));
                if (nbt.getInt("StatLuck") > 0) event.getToolTip().add(new StringTextComponent("§e+" + nbt.getInt("StatLuck") + " Удача"));
                if (nbt.getInt("StatVitality") > 0) event.getToolTip().add(new StringTextComponent("§4+" + nbt.getInt("StatVitality") + " Жизнь"));
            }

            addPriceTooltip(event, stack);
        }
        // === МАГИЧЕСКАЯ ЕДА ===
        else if (nbt.contains("RarityLevel")) {
            int rIndex = Math.min(4, nbt.getInt("RarityLevel"));
            Rarity rarity = Rarity.values()[rIndex];
            int buffId = RpgBuffs.getBuffForFood(stack.getItem());

            float power = RpgBuffs.BASE_POWER[buffId] * RpgBuffs.RARITY_MULTIPLIER[rIndex];
            String desc = RpgBuffs.DESCRIPTIONS[buffId];

            if (desc.contains("%")) {
                int percent = (int) Math.round(power * 100);
                desc = desc.replace("%", "" + percent);
            } else if (desc.contains("HP") || desc.contains("мана")) {
                desc = desc.replace("X", String.format("%.1f", power));
            }

            int itemLevel = nbt.contains("ItemLevel") ? nbt.getInt("ItemLevel") : 1;
            int bonusMinutes = itemLevel / 40;
            int totalMinutes = RpgBuffs.DURATION_MINUTES[rIndex] + bonusMinutes;

            event.getToolTip().add(new StringTextComponent(""));
            event.getToolTip().add(new StringTextComponent(rarity.getColor() + "[" + rarity.getName() + "]"));
            event.getToolTip().add(new StringTextComponent("§b" + RpgBuffs.NAMES[buffId] + " §7— " + desc));
            event.getToolTip().add(new StringTextComponent("§7Длительность: " + totalMinutes + " мин"));

            addPriceTooltip(event, stack);
        }
    }
}