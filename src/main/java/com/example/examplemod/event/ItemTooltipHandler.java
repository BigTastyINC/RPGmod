package com.example.examplemod.event;

import com.example.examplemod.buffs.RpgBuffs;
import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.item.ItemAffix;
import com.example.examplemod.item.ItemStatHelper;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.item.Rarity;
import com.example.examplemod.item.RpgPotionItem;
import com.example.examplemod.item.StaffItem;
import com.example.examplemod.jewelry.JewelryItem;
import com.example.examplemod.jewelry.JewelryType;
import com.example.examplemod.shop.ShopUtils;
import com.example.examplemod.spells.Spell;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "examplemod")
public class ItemTooltipHandler {

    private static String formatFullPrice(int copper) {
        int g = copper / 10000;
        int s = (copper % 10000) / 100;
        int m = copper % 100;
        return g + "з " + s + "с " + m + "м";
    }

    private static void addPriceTooltip(ItemTooltipEvent event, ItemStack stack) {
        int unitPrice = ShopUtils.calcSellPrice(stack);
        int totalPrice = unitPrice * stack.getCount();
        event.getToolTip().add(new StringTextComponent("§8--------------------"));
        event.getToolTip().add(new StringTextComponent("§6Цена: §e" + formatFullPrice(unitPrice)));
        if (stack.getCount() > 1) {
            event.getToolTip().add(new StringTextComponent("§6Цена стака: §e" + formatFullPrice(totalPrice)));
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        CompoundNBT nbt = stack.getTag();
        PlayerEntity player = event.getPlayer();
        if (player == null) return;

        final int[] mageRank = {0};
        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> mageRank[0] = data.getMageRank());

        // === СВИТОК ЗАКЛИНАНИЯ ===
        if (stack.getItem() == ModItems.SPELL_SCROLL.get()) {
            if (nbt != null && nbt.contains("SpellId")) {
                Spell spell = Spell.byId(nbt.getString("SpellId"));
                int lvl = nbt.getInt("SpellLevel");
                int rarityIndex = Math.min(4, nbt.getInt("RarityLevel"));
                Rarity rarity = Rarity.values()[rarityIndex];
                float finalDamage = spell.getDamage(lvl, rarityIndex);
                int manaCost = spell.getManaCost(mageRank[0]);

                event.getToolTip().add(new StringTextComponent(rarity.getColor() + "§l[ " + rarity.getName() + " ] Свиток"));
                event.getToolTip().add(new StringTextComponent("§8--------------------"));
                event.getToolTip().add(new StringTextComponent("§bЗаклинание: §f" + spell.getName()));
                event.getToolTip().add(new StringTextComponent("§7Уровень заклинания: " + lvl));
                event.getToolTip().add(new StringTextComponent("§7" + spell.getDescription()));
                event.getToolTip().add(new StringTextComponent("§8--------------------"));
                event.getToolTip().add(new StringTextComponent("§9Мана: §b" + manaCost));
                if (finalDamage > 0) {
                    event.getToolTip().add(new StringTextComponent("§cУрон: §4" + (int) finalDamage));
                }
                addPriceTooltip(event, stack);
            }
            return;
        }

        // === ЗЕЛЬЕ УДАЧИ ===
        if (stack.getItem() == ModItems.LUCK_POTION.get()) {
            event.getToolTip().add(new StringTextComponent("§6§l[Легендарный] Зелье Удачи"));
            event.getToolTip().add(new StringTextComponent("§8--------------------"));
            event.getToolTip().add(new StringTextComponent("§d✦ Эффекты:"));
            event.getToolTip().add(new StringTextComponent("§d• +50% опыта профессий"));
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
                int rarityIndex = Math.min(4, mageRank[0]);
                Rarity rarity = Rarity.values()[rarityIndex];
                float finalDamage = spell.getDamage(spellLevel, rarityIndex);
                int finalMana = spell.getManaCost(mageRank[0]);

                event.getToolTip().add(new StringTextComponent(rarity.getColor() + "§l[" + rarity.getName() + "] " + spell.getName()));
                event.getToolTip().add(new StringTextComponent("§8Уровень заклинания: " + spellLevel));
                event.getToolTip().add(new StringTextComponent("§8--------------------"));
                event.getToolTip().add(new StringTextComponent("§7" + spell.getDescription()));
                if (finalDamage > 0) {
                    event.getToolTip().add(new StringTextComponent("§cУрон: §4" + (int) finalDamage));
                }
                event.getToolTip().add(new StringTextComponent("§9Мана: §b" + finalMana));
                if (mageRank[0] < spellLevel) {
                    event.getToolTip().add(new StringTextComponent("§4⚠ Требуется Ранг Мага: " + spellLevel));
                }
                addPriceTooltip(event, stack);
            }
            return;
        }

        // === ПОСОХИ ===
        if (stack.getItem() instanceof StaffItem) {
            StaffItem staff = (StaffItem) stack.getItem();
            int level = nbt.getInt("ItemLevel");
            int rIndex = Math.min(4, nbt.getInt("RarityLevel"));
            Rarity rarity = Rarity.values()[rIndex];

            event.getToolTip().add(new StringTextComponent(rarity.getColor() + "§l[" + rarity.getName() + "] " + stack.getHoverName().getString()));
            event.getToolTip().add(new StringTextComponent("§8Уровень предмета: " + level));
            event.getToolTip().add(new StringTextComponent("§8--------------------"));
            event.getToolTip().add(new StringTextComponent("§7Множитель урона: §e" + staff.getDamageMultiplier() + "x"));

            String selected = nbt.getString("SelectedSpell");
            if (!selected.isEmpty()) {
                Spell s = Spell.byId(selected);
                int manaCost = s.getManaCost(mageRank[0]);
                event.getToolTip().add(new StringTextComponent("§bЗаклинание: §f" + s.getName() + " §7(-" + manaCost + " маны)"));
            } else {
                event.getToolTip().add(new StringTextComponent("§7ЛКМ — выбрать заклинание"));
            }

            int maxEnergy = staff.getMaxDamage(stack);
            int currentEnergy = maxEnergy - stack.getDamageValue();
            event.getToolTip().add(new StringTextComponent("§dМагическая энергия: §e" + currentEnergy + " §7/ §e" + maxEnergy));
            event.getToolTip().add(new StringTextComponent("§8--------------------"));

            if (nbt.getInt("StatIntelligence") > 0) event.getToolTip().add(new StringTextComponent("§a+" + nbt.getInt("StatIntelligence") + " Интеллект"));
            if (nbt.getInt("StatLuck") > 0) event.getToolTip().add(new StringTextComponent("§a+" + nbt.getInt("StatLuck") + " Удача"));

            List<ItemAffix> affixes = ItemStatHelper.getAffixes(stack);
            if (!affixes.isEmpty()) {
                event.getToolTip().add(new StringTextComponent("§8--------------------"));
                event.getToolTip().add(new StringTextComponent("§9✦ Эффекты:"));
                for (ItemAffix a : affixes) {
                    event.getToolTip().add(new StringTextComponent("§d• " + a.getName() + ": §7" + a.getDescription()));
                }
            }

            addPriceTooltip(event, stack);
            return;
        }

        // === БИЖУТЕРИЯ ===
        if (stack.getItem() instanceof JewelryItem) {
            JewelryItem jewelry = (JewelryItem) stack.getItem();
            int level = nbt.getInt("ItemLevel");
            int rIndex = Math.min(4, nbt.getInt("RarityLevel"));
            Rarity rarity = Rarity.values()[rIndex];

            event.getToolTip().add(new StringTextComponent(rarity.getColor() + "§l[" + rarity.getName() + "] " + stack.getHoverName().getString()));
            event.getToolTip().add(new StringTextComponent("§8Уровень предмета: " + level));
            event.getToolTip().add(new StringTextComponent("§7Тип: " + jewelry.getJewelryType().getDisplayName()));
            event.getToolTip().add(new StringTextComponent("§8--------------------"));

            if (nbt.getInt("StatStrength") > 0) event.getToolTip().add(new StringTextComponent("§a+" + nbt.getInt("StatStrength") + " Сила"));
            if (nbt.getInt("StatDexterity") > 0) event.getToolTip().add(new StringTextComponent("§a+" + nbt.getInt("StatDexterity") + " Ловкость"));
            if (nbt.getInt("StatIntelligence") > 0) event.getToolTip().add(new StringTextComponent("§a+" + nbt.getInt("StatIntelligence") + " Интеллект"));
            if (nbt.getInt("StatLuck") > 0) event.getToolTip().add(new StringTextComponent("§a+" + nbt.getInt("StatLuck") + " Удача"));
            if (nbt.getInt("StatVitality") > 0) event.getToolTip().add(new StringTextComponent("§a+" + nbt.getInt("StatVitality") + " Жизнь"));

            List<ItemAffix> affixes = ItemStatHelper.getAffixes(stack);
            if (!affixes.isEmpty()) {
                event.getToolTip().add(new StringTextComponent("§8--------------------"));
                event.getToolTip().add(new StringTextComponent("§9✦ Эффекты:"));
                for (ItemAffix a : affixes) {
                    event.getToolTip().add(new StringTextComponent("§d• " + a.getName() + ": §7" + a.getDescription()));
                }
            }

            addPriceTooltip(event, stack);
            return;
        }

        // === РЕЛИКВИИ ===
        if (nbt.contains("RelicBasePrice")) {
            int rarityIndex = Math.min(4, nbt.getInt("RarityLevel"));
            Rarity rarity = Rarity.values()[rarityIndex];
            event.getToolTip().add(new StringTextComponent(rarity.getColor() + "§l[" + rarity.getName() + "] Реликвия"));
            event.getToolTip().add(new StringTextComponent("§8--------------------"));
            event.getToolTip().add(new StringTextComponent("§6Уникальный предмет!"));
            addPriceTooltip(event, stack);
            return;
        }

        // === МАГИЧЕСКАЯ ЕДА ===
        if (FoodBuffHandler.isPreparedFood(stack.getItem()) && nbt.contains("RarityLevel")) {
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

            event.getToolTip().add(new StringTextComponent(rarity.getColor() + "§l[" + rarity.getName() + "] " + stack.getHoverName().getString()));
            event.getToolTip().add(new StringTextComponent("§8--------------------"));
            event.getToolTip().add(new StringTextComponent("§9✦ Эффект:"));
            event.getToolTip().add(new StringTextComponent("§d• " + RpgBuffs.NAMES[buffId] + ": §7" + desc));
            event.getToolTip().add(new StringTextComponent("§7Длительность: " + totalMinutes + " мин"));
            addPriceTooltip(event, stack);
            return;
        }

        // === ОРУЖИЕ / БРОНЯ / ЗЕЛЬЯ / КРИСТАЛЛЫ ===
        if (nbt.contains("ItemLevel")) {
            int level = nbt.getInt("ItemLevel");
            int rIndex = Math.min(4, nbt.getInt("RarityLevel"));
            Rarity rarity = Rarity.values()[rIndex];

            event.getToolTip().add(new StringTextComponent(rarity.getColor() + "§l[" + rarity.getName() + "] " + stack.getHoverName().getString()));
            event.getToolTip().add(new StringTextComponent("§8Уровень предмета: " + level));
            event.getToolTip().add(new StringTextComponent("§8--------------------"));

            if (stack.getItem() instanceof RpgPotionItem) {
                RpgPotionItem potion = (RpgPotionItem) stack.getItem();
                int amount = (int) (level * 5 * rarity.getMultiplier());
                if (potion.isManaPotion()) event.getToolTip().add(new StringTextComponent("§bВосстанавливает " + amount + " маны"));
                else event.getToolTip().add(new StringTextComponent("§cВосстанавливает " + amount + " здоровья"));
            } else if (stack.getItem() == ModItems.SMALL_MAGIC_CRYSTAL.get() ||
                    stack.getItem() == ModItems.MEDIUM_MAGIC_CRYSTAL.get() ||
                    stack.getItem() == ModItems.LARGE_MAGIC_CRYSTAL.get()) {
                int energy = (int) (level * 10 * rarity.getMultiplier());
                event.getToolTip().add(new StringTextComponent("§dМагическая энергия: " + energy));
            } else if (!stack.getItem().isEdible()) {
                if (nbt.getInt("StatStrength") > 0) event.getToolTip().add(new StringTextComponent("§a+" + nbt.getInt("StatStrength") + " Сила"));
                if (nbt.getInt("StatDexterity") > 0) event.getToolTip().add(new StringTextComponent("§a+" + nbt.getInt("StatDexterity") + " Ловкость"));
                if (nbt.getInt("StatIntelligence") > 0) event.getToolTip().add(new StringTextComponent("§a+" + nbt.getInt("StatIntelligence") + " Интеллект"));
                if (nbt.getInt("StatLuck") > 0) event.getToolTip().add(new StringTextComponent("§a+" + nbt.getInt("StatLuck") + " Удача"));
                if (nbt.getInt("StatVitality") > 0) event.getToolTip().add(new StringTextComponent("§a+" + nbt.getInt("StatVitality") + " Жизнь"));

                List<ItemAffix> affixes = ItemStatHelper.getAffixes(stack);
                if (!affixes.isEmpty() || ItemStatHelper.hasSilverBonus(stack) || ItemStatHelper.hasOrichalcumBonus(stack)) {
                    event.getToolTip().add(new StringTextComponent("§8--------------------"));
                    event.getToolTip().add(new StringTextComponent("§9✦ Эффекты:"));
                    for (ItemAffix a : affixes) {
                        event.getToolTip().add(new StringTextComponent("§d• " + a.getName() + ": §7" + a.getDescription()));
                    }
                    if (ItemStatHelper.hasSilverBonus(stack)) {
                        event.getToolTip().add(new StringTextComponent("§d• Серебро: §7+20% урона по нежити"));
                    }
                    if (ItemStatHelper.hasOrichalcumBonus(stack)) {
                        event.getToolTip().add(new StringTextComponent("§d• Орихалк: §7+5% вампиризма"));
                    }
                }
            }
            addPriceTooltip(event, stack);
        }
    }
}