package com.example.examplemod.profession;

import net.minecraft.nbt.CompoundNBT;

public class ProfessionData {
    private final int maxLevel;
    private final int xpMultiplier;

    private int level = 1;
    private int xp = 0;

    public ProfessionData(int xpMultiplier, int maxLevel) {
        this.xpMultiplier = xpMultiplier;
        this.maxLevel = maxLevel;
    }

    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public int getMaxLevel() { return maxLevel; }
    public boolean isMaxLevel() { return level >= maxLevel; }

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
        this.level = Math.max(1, Math.min(maxLevel, nbt.getInt("level")));
        this.xp = Math.max(0, nbt.getInt("xp"));
    }
}