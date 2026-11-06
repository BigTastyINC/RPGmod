package com.example.examplemod.profession;

import net.minecraft.nbt.CompoundNBT;

public class ProfessionData {
    public static final int MAX_LEVEL = 100;

    private int level = 1;
    private int xp = 0;
    private final int xpMultiplier;

    public ProfessionData(int xpMultiplier) {
        this.xpMultiplier = xpMultiplier;
    }

    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public boolean isMaxLevel() { return level >= MAX_LEVEL; }

    // Формула: уровень * 25 * коэффициент (3 для крафтов, 1 для остальных)
    public int getXpForNextLevel() { return level * 25 * xpMultiplier; }

    public boolean addXp(int amount) {
        if (isMaxLevel()) return false;
        xp += amount;
        boolean leveled = false;
        while (!isMaxLevel() && xp >= getXpForNextLevel()) {
            xp -= getXpForNextLevel();
            level++;
            leveled = true;
        }
        if (isMaxLevel()) xp = 0;
        return leveled;
    }

    public void writeToNBT(CompoundNBT nbt) {
        nbt.putInt("level", level);
        nbt.putInt("xp", xp);
    }

    public void readFromNBT(CompoundNBT nbt) {
        this.level = Math.max(1, Math.min(MAX_LEVEL, nbt.getInt("level")));
        this.xp = Math.max(0, nbt.getInt("xp"));
    }
}