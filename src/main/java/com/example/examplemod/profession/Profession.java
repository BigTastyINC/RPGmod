package com.example.examplemod.profession;

public enum Profession {
    // 3 = прокачка в 3 раза сложнее, 1 = обычная
    WEAPONSMITH("weaponsmith", "Оружейник", 3),
    ARMORSMITH("armorsmith", "Бронник", 3),
    ALCHEMIST("alchemist", "Алхимик", 3),
    FISHERMAN("fisherman", "Рыбак", 1),
    MINER("miner", "Рудокоп", 1);

    private final String id;
    private final String displayName;
    private final int xpMultiplier;

    Profession(String id, String displayName, int xpMultiplier) {
        this.id = id;
        this.displayName = displayName;
        this.xpMultiplier = xpMultiplier;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getXpMultiplier() { return xpMultiplier; }

    public static Profession byId(String id) {
        for (Profession p : values()) if (p.id.equals(id)) return p;
        return WEAPONSMITH;
    }
}