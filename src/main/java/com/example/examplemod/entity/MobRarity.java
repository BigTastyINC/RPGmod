package com.example.examplemod.entity;

public enum MobRarity {
    COMMON("", 1.0, 1.0, 1.0),
    LEADER("§6[Предводитель] ", 2.5, 1.5, 2.0),
    LORD("§4[Лорд] ", 5.0, 2.5, 5.0);

    private final String prefix;
    private final double healthMultiplier;
    private final double damageMultiplier;
    private final double xpMultiplier;

    MobRarity(String prefix, double healthMultiplier, double damageMultiplier, double xpMultiplier) {
        this.prefix = prefix;
        this.healthMultiplier = healthMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.xpMultiplier = xpMultiplier;
    }

    public String getPrefix() { return prefix; }
    public double getHealthMultiplier() { return healthMultiplier; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public double getXpMultiplier() { return xpMultiplier; }
}