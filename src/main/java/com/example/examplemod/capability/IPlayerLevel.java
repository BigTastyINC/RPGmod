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

    int getHealthRegen();
    void setHealthRegen(int value);
    int getManaRegen();
    void setManaRegen(int value);

    // ===== СТАМИНА =====
    int getStamina();
    void setStamina(int stamina);
    int getStaminaRegen();
    void setStaminaRegen(int value);
    int getStaminaRegenDelay();
    void setStaminaRegenDelay(int delay);

    // Максимальная стамина зависит от силы: 100 + 3 за каждое очко силы выше 1
    default int getMaxStamina() {
        return 100 + (getStrength() - 1) * 3;
    }

    int getMana();
    void setMana(int mana);
    default int getMaxMana() {
        return 100 + (getIntelligence() - 1) * 10;
    }

    int getCopper();
    void setCopper(int copper);
    int getSilver();
    void setSilver(int silver);
    int getGold();
    void setGold(int gold);
    void addCoins(int copperAmount);

    void setBuff(int buffId, long endTime, float power);
    long getBuffEnd(int buffId);
    float getBuffPower(int buffId);
    boolean hasBuff(int buffId);
    void clearBuff(int buffId);
    CompoundNBT getAllBuffs();

    CompoundNBT serializeNBT();
    void deserializeNBT(CompoundNBT nbt);
}