package com.example.examplemod.profession;

public enum Profession {
    WEAPONSMITH("weaponsmith", "Оружейник", 3, 100),
    ARMORSMITH("armorsmith", "Бронник", 3, 100),
    ALCHEMIST("alchemist", "Алхимик", 3, 100),
    FISHERMAN("fisherman", "Рыбак", 1, 9999), // Безлимит
    MINER("miner", "Рудокоп", 1, 100);

    private final String id;
    private final String displayName;
    private final int xpMultiplier;
    private final int maxLevel;

    Profession(String id, String displayName, int xpMultiplier, int maxLevel) {
        this.id = id;
        this.displayName = displayName;
        this.xpMultiplier = xpMultiplier;
        this.maxLevel = maxLevel;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getXpMultiplier() { return xpMultiplier; }
    public int getMaxLevel() { return maxLevel; }

    public static Profession byId(String id) {
        for (Profession p : values()) if (p.id.equals(id)) return p;
        return WEAPONSMITH;
    }
}