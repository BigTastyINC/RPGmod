package com.example.examplemod.entity;

public enum MobRarity {
    // Животные (звезды)
    COMMON("", 1.0, 1.0, 1.0, 0),
    UNCOMMON("", 1.3, 1.1, 1.5, 1),
    RARE("", 1.8, 1.3, 2.0, 2),
    EPIC("", 2.5, 1.6, 3.0, 3),
    LEGENDARY("", 4.0, 2.0, 5.0, 4),

    // Монстры (скобки, без пробела в конце)
    LEADER("§6[Предводитель]", 2.5, 1.5, 2.0, 0),
    LORD("§4[Лорд]", 5.0, 2.5, 5.0, 0);

    private final String prefix;
    private final double healthMultiplier;
    private final double damageMultiplier;
    private final double xpMultiplier;
    private final int starCount;

    MobRarity(String prefix, double healthMultiplier, double damageMultiplier, double xpMultiplier, int starCount) {
        this.prefix = prefix;
        this.healthMultiplier = healthMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.xpMultiplier = xpMultiplier;
        this.starCount = starCount;
    }

    public double getHealthMultiplier() { return healthMultiplier; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public double getXpMultiplier() { return xpMultiplier; }
    public String getPrefix() { return prefix; }

    public String getStars() {
        if (starCount == 0) return "";
        StringBuilder stars = new StringBuilder(" ");
        if (this == UNCOMMON) stars.append("§a");
        else if (this == RARE) stars.append("§9");
        else if (this == EPIC) stars.append("§5");
        else if (this == LEGENDARY) stars.append("§6");

        for (int i = 0; i < starCount; i++) {
            stars.append("★");
        }
        return stars.toString();
    }
}