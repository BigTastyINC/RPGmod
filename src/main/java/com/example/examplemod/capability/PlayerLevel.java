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

    private int healthRegen = 0;
    private int manaRegen = 0;

    // Стамина
    private int stamina = 100;
    private int maxStamina = 100;
    private int staminaRegenDelay = 0;

    // Монеты
    private int copper = 0;
    private int silver = 0;
    private int gold = 0;

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

    // --- СТАМИНА ---
    @Override public int getStamina() { return stamina; }
    @Override public void setStamina(int stamina) { this.stamina = Math.min(stamina, maxStamina); }
    @Override public int getMaxStamina() { return maxStamina; }
    @Override public int getStaminaRegenDelay() { return staminaRegenDelay; }
    @Override public void setStaminaRegenDelay(int delay) { this.staminaRegenDelay = delay; }

    // --- МОНЕТЫ ---
    @Override public int getCopper() { return copper; }
    @Override public void setCopper(int copper) { this.copper = copper; }
    @Override public int getSilver() { return silver; }
    @Override public void setSilver(int silver) { this.silver = silver; }
    @Override public int getGold() { return gold; }
    @Override public void setGold(int gold) { this.gold = gold; }

    @Override
    public void addCoins(int copperAmount) {
        this.copper += copperAmount;
        if (this.copper >= 100) {
            this.silver += this.copper / 100;
            this.copper = this.copper % 100;
        }
        if (this.silver >= 100) {
            this.gold += this.silver / 100;
            this.silver = this.silver % 100;
        }
    }

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
        nbt.putInt("stamina", stamina);
        nbt.putInt("staminaRegenDelay", staminaRegenDelay);
        nbt.putInt("copper", copper);
        nbt.putInt("silver", silver);
        nbt.putInt("gold", gold);
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
        this.stamina = nbt.contains("stamina") ? nbt.getInt("stamina") : 100;
        this.staminaRegenDelay = nbt.getInt("staminaRegenDelay");
        this.copper = nbt.getInt("copper");
        this.silver = nbt.getInt("silver");
        this.gold = nbt.getInt("gold");
    }
}