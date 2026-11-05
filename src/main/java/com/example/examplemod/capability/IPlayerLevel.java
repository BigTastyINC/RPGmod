package com.example.examplemod.capability;

import net.minecraft.nbt.CompoundNBT;

public interface IPlayerLevel {
    int getLevel();
    void setLevel(int level);
    int getXp();
    void setXp(int xp);
    void addXp(int amount);

    int getStatPoints();
    void setStatPoints(int points);
    void addStatPoints(int points);

    int getStrength();
    void setStrength(int value);

    int getDexterity();
    void setDexterity(int value);

    int getIntelligence();
    void setIntelligence(int value);

    int getLuck();
    void setLuck(int value);

    int getVitality();
    void setVitality(int value);

    // --- НОВЫЕ СТАТЫ ---
    int getHealthRegen();
    void setHealthRegen(int value);

    int getManaRegen();
    void setManaRegen(int value);

    // МАНА
    int getMana();
    void setMana(int mana);
    default int getMaxMana() {
        return 100 + (getIntelligence() - 1) * 10;
    }

    CompoundNBT serializeNBT();
    void deserializeNBT(CompoundNBT nbt);
}