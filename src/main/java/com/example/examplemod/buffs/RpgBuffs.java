package com.example.examplemod.buffs;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class RpgBuffs {

    public static final int XP_BOOST = 0;
    public static final int DAMAGE_RESIST = 1;
    public static final int HEALTH_REGEN = 2;
    public static final int MANA_REGEN = 3;
    public static final int LOOT_LUCK = 4;
    public static final int ORE_LUCK = 5;
    public static final int BUFF_COUNT = 6;

    public static final String[] NAMES = {
            "Жажда битвы",
            "Каменная кожа",
            "Дар природы",
            "Мудрость",
            "Охотник",
            "Рудокоп"
    };

    public static final String[] DESCRIPTIONS = {
            "+% опыта с мобов",
            "-% получаемого урона",
            "+HP регенерация в секунду",
            "+мана регенерация в секунду",
            "+% шанс доп. дропа с мобов",
            "+% шанс доп. дропа с руды"
    };

    // Базовая сила баффа ДО множителя редкости
    public static final float[] BASE_POWER = {
            0.25f,  // XP_BOOST: +25% опыта
            0.10f,  // DAMAGE_RESIST: -10% урона
            0.5f,   // HEALTH_REGEN: +0.5 HP/сек
            1.0f,   // MANA_REGEN: +1 мана/сек
            0.20f,  // LOOT_LUCK: +20% шанс доп. дропа
            0.20f   // ORE_LUCK: +20% шанс доп. дропа
    };

    public static final int[] DURATION_MINUTES = {5, 7, 9, 12, 15};
    public static final float[] RARITY_MULTIPLIER = {1.0f, 1.25f, 1.5f, 2.0f, 3.0f};

    public static int getBuffForFood(Item item) {
        // Рыба → Жажда битвы
        if (item == Items.COOKED_COD || item == Items.COOKED_SALMON) return XP_BOOST;

        // Мясо → Каменная кожа
        if (item == Items.COOKED_BEEF || item == Items.COOKED_PORKCHOP
                || item == Items.COOKED_MUTTON || item == Items.COOKED_CHICKEN
                || item == Items.COOKED_RABBIT) return DAMAGE_RESIST;

        // Хлеб, печёная картошка, пирог → Дар природы
        if (item == Items.BREAD || item == Items.BAKED_POTATO
                || item == Items.PUMPKIN_PIE || item == Items.DRIED_KELP) return HEALTH_REGEN;

        // Печенье, золотая морковь, медовый напиток → Мудрость
        if (item == Items.COOKIE || item == Items.GOLDEN_CARROT
                || item == Items.HONEY_BOTTLE) return MANA_REGEN;

        // Золотое яблоко → Охотник
        if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) return LOOT_LUCK;

        // Похлёбки → Рудокоп
        if (item == Items.MUSHROOM_STEW || item == Items.BEETROOT_SOUP
                || item == Items.RABBIT_STEW || item == Items.SUSPICIOUS_STEW) return ORE_LUCK;

        // Любая другая еда (например, модовая) — Жажда битвы по умолчанию
        return XP_BOOST;
    }
}