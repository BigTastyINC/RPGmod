package com.example.examplemod.spells;

public enum Spell {
    // ID, Название, Описание, Базовый урон, Затраты маны, Врожденный уровень (0-5)
    FIREBALL("fireball", "Огненный шар", "Запускает шар огня, поджигая цель.", 10, 20, 0),
    HEAL("heal", "Исцеление", "Восстанавливает здоровье магической энергией.", 15, 30, 1),
    FROSTBOLT("frostbolt", "Ледяная стрела", "Замедляет цель и наносит урон холодом.", 8, 15, 2);

    private final String id;
    private final String name;
    private final String description;
    private final int baseDamage;
    private final int baseManaCost;
    private final int defaultLevel; // ФИКСИРОВАННЫЙ УРОВЕНЬ

    Spell(String id, String name, String description, int baseDamage, int baseManaCost, int defaultLevel) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseDamage = baseDamage;
        this.baseManaCost = baseManaCost;
        this.defaultLevel = defaultLevel;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getBaseDamage() { return baseDamage; }
    public int getBaseManaCost() { return baseManaCost; }
    public int getDefaultLevel() { return defaultLevel; }

    public static Spell byId(String id) {
        for (Spell s : values()) {
            if (s.id.equals(id)) return s;
        }
        return FIREBALL;
    }
}