package com.example.examplemod.capability;

import net.minecraft.nbt.CompoundNBT;

public class PlayerLevel implements IPlayerLevel {
    private int level = 1;
    private int xp = 0;
    private int statPoints = 0;
    private int mana = 100;

    private int strength = 1;
    private int dexterity = 1;
    private int intelligence = 1;
    private int luck = 1;
    private int vitality = 1;

    // НОВЫЕ СТАТЫ (начинаем с 0)
    private int healthRegen = 0;
    private int manaRegen = 0;

    @Override public int getLevel() { return level; }
    @Override public void setLevel(int level) { this.level = level; }
    @Override public int getXp() { return xp; }
    @Override public void setXp(int xp) { this.xp = xp; }

    @Override public int getStatPoints() { return statPoints; }
    @Override public void setStatPoints(int points) { this.statPoints = points; }
    @Override public void addStatPoints(int points) { this.statPoints += points; }

    @Override public int getStrength() { return strength; }
    @Override public void setStrength(int value) { this.strength = value; }

    @Override public int getDexterity() { return dexterity; }
    @Override public void setDexterity(int value) { this.dexterity = value; }

    @Override public int getIntelligence() { return intelligence; }
    @Override public void setIntelligence(int value) { this.intelligence = value; }

    @Override public int getLuck() { return luck; }
    @Override public void setLuck(int value) { this.luck = value; }

    @Override public int getVitality() { return vitality; }
    @Override public void setVitality(int value) { this.vitality = value; }

    @Override public int getHealthRegen() { return healthRegen; }
    @Override public void setHealthRegen(int value) { this.healthRegen = value; }

    @Override public int getManaRegen() { return manaRegen; }
    @Override public void setManaRegen(int value) { this.manaRegen = value; }

    @Override public int getMana() { return mana; }
    @Override public void setMana(int mana) { this.mana = Math.min(mana, getMaxMana()); }

    @Override
    public void addXp(int amount) {
        this.xp += amount;
        int xpNeeded = this.level * 100;

        while (this.xp >= xpNeeded) {
            this.xp -= xpNeeded;
            this.level++;
            this.statPoints += 3;
            xpNeeded = this.level * 100;
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("level", level);
        nbt.putInt("xp", xp);
        nbt.putInt("statPoints", statPoints);
        nbt.putInt("mana", mana);
        nbt.putInt("strength", strength);
        nbt.putInt("dexterity", dexterity);
        nbt.putInt("intelligence", intelligence);
        nbt.putInt("luck", luck);
        nbt.putInt("vitality", vitality);
        nbt.putInt("healthRegen", healthRegen);
        nbt.putInt("manaRegen", manaRegen);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.level = nbt.getInt("level");
        this.xp = nbt.getInt("xp");
        this.statPoints = nbt.getInt("statPoints");
        this.mana = nbt.contains("mana") ? nbt.getInt("mana") : getMaxMana();
        this.strength = nbt.getInt("strength");
        this.dexterity = nbt.getInt("dexterity");
        this.intelligence = nbt.getInt("intelligence");
        this.luck = nbt.getInt("luck");
        this.vitality = nbt.getInt("vitality");
        this.healthRegen = nbt.getInt("healthRegen");
        this.manaRegen = nbt.getInt("manaRegen");
    }
}