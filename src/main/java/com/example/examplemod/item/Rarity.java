package com.example.examplemod.item;

public enum Rarity {
    COMMON("Обычный", "§7", 1.0),
    UNCOMMON("Необычный", "§a", 1.5),
    RARE("Редкий", "§9", 2.0),
    EPIC("Эпический", "§5", 3.0),
    LEGENDARY("Легендарный", "§6", 5.0);

    private final String name;
    private final String color;
    private final double multiplier;

    Rarity(String name, String color, double multiplier) {
        this.name = name;
        this.color = color;
        this.multiplier = multiplier;
    }

    public String getName() { return name; }
    public String getColor() { return color; }
    public double getMultiplier() { return multiplier; }
}