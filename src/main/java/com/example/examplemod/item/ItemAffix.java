package com.example.examplemod.item;

public enum ItemAffix {
    // === ОРУЖИЕ ===
    BLEEDING("bleeding", "Кровотечение", "15% шанс: 3 HP/сек в течение 3 сек.", Type.WEAPON),
    CRIT("crit", "Критический удар", "10% шанс нанести двойной урон", Type.WEAPON),
    VAMPIRISM("vampirism", "Вампиризм", "5% нанесённого урона возвращается в HP", Type.WEAPON),
    FIRE_BLADE("fire_blade", "Огненный клинок", "Поджигает цель на 3 сек.", Type.WEAPON),
    SLOWING("slowing", "Замедление", "20% шанс замедлить цель на 2 сек.", Type.WEAPON),
    CRUSHING("crushing", "Сокрушение", "10% шанс оглушить цель на 1 сек.", Type.WEAPON),
    BLOODLUST("bloodlust", "Жажда крови", "+15% урона, но -10% макс. HP", Type.WEAPON),

    // === БРОНЯ ===
    RESILIENCE("resilience", "Стойкость", "+15 к максимальной стамине", Type.ARMOR),
    REGENERATION("regeneration", "Регенерация", "+20% к скорости регенерации HP", Type.ARMOR),
    REFLECTION("reflection", "Отражение", "10% полученного урона возвращается атакующему", Type.ARMOR),
    UNBREAKABLE("unbreakable", "Непоколебимость", "Иммунитет к отбрасыванию", Type.ARMOR),
    SWIFTNESS("swiftness", "Быстрота", "+5% к скорости передвижения", Type.ARMOR),
    PROTECTION_AURA("protection_aura", "Аура защиты", "-10% получаемого урона", Type.ARMOR),

    // === ПОСОХИ ===
    MANA_RESERVE("mana_reserve", "Магический резерв", "+15% к максимальной мане", Type.STAFF),
    MANA_FLOW("mana_flow", "Поток маны", "+30% к регенерации маны", Type.STAFF),
    MANA_SAVING("mana_saving", "Экономия", "-10% к затратам маны", Type.STAFF),
    SPELL_POWER("spell_power", "Усиление", "+15% к урону заклинаний", Type.STAFF),
    DOUBLE_CAST("double_cast", "Двойной каст", "5% шанс не потратить ману при касте", Type.STAFF),

    // === БИЖУТЕРИЯ ===
    DODGE("dodge", "Уклонение", "10% шанс полностью избежать урона", Type.JEWELRY),
    REGEN_AURA("regen_aura", "Аура регенерации", "+1 HP/сек пассивно", Type.JEWELRY),
    POISON("poison", "Отрава", "15% шанс отравить атакующего", Type.JEWELRY),
    AGILITY("agility", "Проворство", "+5% скорости передвижения", Type.JEWELRY);

    public enum Type { WEAPON, ARMOR, STAFF, JEWELRY }

    private final String id;
    private final String name;
    private final String description;
    private final Type type;

    ItemAffix(String id, String name, String description, Type type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Type getType() { return type; }

    public static ItemAffix byId(String id) {
        for (ItemAffix a : values()) if (a.id.equals(id)) return a;
        return null;
    }
}