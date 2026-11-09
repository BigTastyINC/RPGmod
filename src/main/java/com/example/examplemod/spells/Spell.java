package com.example.examplemod.spells;

import com.example.examplemod.item.Rarity;

public enum Spell {
    // ID, Название, Описание, Урон, Мана, Уровень, Тип масштабирования маны
    FIREBALL("fireball", "Огненный шар", "Запускает шар огня, поджигая цель.", 10, 20, 0, ManaScaling.EXPONENTIAL),
    HEAL("heal", "Исцеление", "Восстанавливает здоровье магической энергией.", 15, 30, 1, ManaScaling.EXPONENTIAL),
    FROSTBOLT("frostbolt", "Ледяная стрела", "Замедляет цель и наносит урон холодом.", 8, 15, 2, ManaScaling.EXPONENTIAL),
    TELEPORT_ORE("teleport_ore", "Телепорт руды", "Мгновенно притягивает руду к игроку (до 40 блоков).", 0, 1000, 4, ManaScaling.FIXED),
    CRAFT_RELIC("craft_relic", "Крафт реликвии", "Создаёт алмаз-реликвию со случайной ценностью.", 0, 15000, 5, ManaScaling.FIXED),
    SUMMON_MERCHANT("summon_merchant", "Призыв торговца", "Призывает торговца-мага к вам.", 0, 150, 1, ManaScaling.FIXED),
    // ИЗМЕНЕНО: 1 уровень, линейный рост маны (+500 за ранг)
    SUMMON_UNDEAD("summon_undead", "Призыв нежити", "Призывает нежить, что сражается на вашей стороне (5 мин).", 0, 500, 1, ManaScaling.LINEAR);

    public enum ManaScaling {
        FIXED,        // Мана не растет (1000 на любом ранге)
        LINEAR,       // baseManaCost * ранг мага (500 → 1000 → 1500 → ...)
        EXPONENTIAL   // baseManaCost * 2^ранг мага (20 → 40 → 80 → ...)
    }

    private final String id;
    private final String name;
    private final String description;
    private final int baseDamage;
    private final int baseManaCost;
    private final int defaultLevel;
    private final ManaScaling manaScaling;

    Spell(String id, String name, String description, int baseDamage, int baseManaCost, int defaultLevel, ManaScaling manaScaling) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseDamage = baseDamage;
        this.baseManaCost = baseManaCost;
        this.defaultLevel = defaultLevel;
        this.manaScaling = manaScaling;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getBaseDamage() { return baseDamage; }
    public int getBaseManaCost() { return baseManaCost; }
    public int getDefaultLevel() { return defaultLevel; }

    public int getManaCost(int mageRank) {
        switch (manaScaling) {
            case FIXED:
                return baseManaCost;
            case LINEAR:
                // На 1 ранге = baseManaCost, на 2 = 2*baseManaCost и т.д.
                // Math.max защищает от 0 на 0 ранге
                return baseManaCost * Math.max(1, mageRank);
            case EXPONENTIAL:
            default:
                return baseManaCost * (int) Math.pow(2, mageRank);
        }
    }

    public static Spell byId(String id) {
        for (Spell s : values()) {
            if (s.id.equals(id)) return s;
        }
        return FIREBALL;
    }

    public float getDamage(int spellLevel, int rarityIndex) {
        int r = Math.max(0, Math.min(4, rarityIndex));
        if (this == FIREBALL) {
            float[] fireballTable = {10f, 45f, 155f, 455f, 800f};
            return fireballTable[r] * (float) Math.pow(2, spellLevel);
        }
        float rarityMult = (float) Rarity.values()[r].getMultiplier();
        return (float) (baseDamage * Math.pow(2, spellLevel) * rarityMult);
    }
}