package com.example.examplemod.capability;

import com.example.examplemod.profession.Profession;
import com.example.examplemod.profession.ProfessionData;
import com.example.examplemod.quest.Quest;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PlayerLevel implements IPlayerLevel {
    private int level = 1, xp = 0, statPoints = 0, mana = 100;
    private int strength = 1, dexterity = 1, intelligence = 1, luck = 1, vitality = 1;
    private int healthRegen = 0, manaRegen = 0, stamina = 100, staminaRegen = 0, staminaRegenDelay = 0;
    private int copper = 0, silver = 0, gold = 0;
    private CompoundNBT buffs = new CompoundNBT();

    private int mageRank = 0, mageXp = 0;
    private final Map<String, Integer> knownSpells = new java.util.HashMap<>();
    private final List<Quest> activeQuests = new ArrayList<>();

    private final Map<Profession, ProfessionData> professions = new EnumMap<>(Profession.class);

    public PlayerLevel() {
        for (Profession p : Profession.values()) professions.put(p, new ProfessionData(p.getXpMultiplier(), p.getMaxLevel()));
    }

    // ... (все существующие геттеры и сеттеры без изменений)
    @Override public int getLevel() { return level; } @Override public void setLevel(int level) { this.level = Math.max(1, level); }
    @Override public int getXp() { return xp; } @Override public void setXp(int xp) { this.xp = xp; }
    @Override public int getStatPoints() { return statPoints; } @Override public void setStatPoints(int points) { this.statPoints = points; } @Override public void addStatPoints(int points) { this.statPoints += points; }
    @Override public int getStrength() { return strength; } @Override public void setStrength(int value) { this.strength = value; }
    @Override public int getDexterity() { return dexterity; } @Override public void setDexterity(int value) { this.dexterity = value; }
    @Override public int getIntelligence() { return intelligence; } @Override public void setIntelligence(int value) { this.intelligence = value; }
    @Override public int getLuck() { return luck; } @Override public void setLuck(int value) { this.luck = value; }
    @Override public int getVitality() { return vitality; } @Override public void setVitality(int value) { this.vitality = value; }
    @Override public int getHealthRegen() { return healthRegen; } @Override public void setHealthRegen(int value) { this.healthRegen = value; }
    @Override public int getManaRegen() { return manaRegen; } @Override public void setManaRegen(int value) { this.manaRegen = value; }
    @Override public int getStamina() { return stamina; } @Override public void setStamina(int stamina) { this.stamina = Math.min(stamina, getMaxStamina()); }
    @Override public int getStaminaRegen() { return staminaRegen; } @Override public void setStaminaRegen(int value) { this.staminaRegen = value; }
    @Override public int getStaminaRegenDelay() { return staminaRegenDelay; } @Override public void setStaminaRegenDelay(int delay) { this.staminaRegenDelay = delay; }
    @Override public int getMaxStamina() { return IPlayerLevel.super.getMaxStamina(); }
    @Override public int getMana() { return mana; } @Override public void setMana(int mana) { this.mana = Math.min(mana, getMaxMana()); }
    @Override public int getCopper() { return copper; } @Override public void setCopper(int copper) { this.copper = copper; }
    @Override public int getSilver() { return silver; } @Override public void setSilver(int silver) { this.silver = silver; }
    @Override public int getGold() { return gold; } @Override public void setGold(int gold) { this.gold = gold; }
    @Override public void addCoins(int copperAmount) { this.copper += copperAmount; if (this.copper >= 100) { this.silver += this.copper / 100; this.copper = this.copper % 100; } if (this.silver >= 100) { this.gold += this.silver / 100; this.silver = this.silver % 100; } }
    @Override public void addXp(int amount) { this.xp += amount; int xpNeeded = this.level * 100; while (this.xp >= xpNeeded) { this.xp -= xpNeeded; this.level++; this.statPoints += 3; xpNeeded = this.level * 100; } }
    private ProfessionData profData(Profession p) { return professions.computeIfAbsent(p, k -> new ProfessionData(p.getXpMultiplier(), p.getMaxLevel())); }
    @Override public int getProfessionLevel(Profession p) { return profData(p).getLevel(); }
    @Override public int getProfessionXp(Profession p) { return profData(p).getXp(); }
    @Override public void setProfessionLevel(Profession p, int level) { CompoundNBT n = new CompoundNBT(); n.putInt("level", level); n.putInt("xp", profData(p).getXp()); profData(p).readFromNBT(n); }
    @Override public void setProfessionXp(Profession p, int xp) { CompoundNBT n = new CompoundNBT(); n.putInt("level", profData(p).getLevel()); n.putInt("xp", xp); profData(p).readFromNBT(n); }
    @Override public boolean addProfessionXp(Profession p, int amount) { return profData(p).addXp(amount); }
    @Override public void setBuff(int buffId, long endTime, float power) { CompoundNBT b = new CompoundNBT(); b.putLong("end", endTime); b.putFloat("power", power); buffs.put("b" + buffId, b); }
    @Override public long getBuffEnd(int buffId) { return buffs.getCompound("b" + buffId).getLong("end"); }
    @Override public float getBuffPower(int buffId) { return buffs.getCompound("b" + buffId).getFloat("power"); }
    @Override public boolean hasBuff(int buffId) { return buffs.contains("b" + buffId); }
    @Override public void clearBuff(int buffId) { buffs.remove("b" + buffId); }
    @Override public CompoundNBT getAllBuffs() { return buffs; }
    @Override public int getMageRank() { return mageRank; } @Override public void setMageRank(int rank) { this.mageRank = Math.max(0, Math.min(5, rank)); }
    @Override public int getMageXp() { return mageXp; } @Override public void setMageXp(int xp) { this.mageXp = xp; }
    @Override public void addMageXp(int amount) {
        if (mageRank >= 5) return;
        this.mageXp += amount;
        int xpNeeded = 500 * (int)Math.pow(2, mageRank);
        while (mageRank < 5 && this.mageXp >= xpNeeded) {
            this.mageXp -= xpNeeded; this.mageRank++; xpNeeded = 500 * (int)Math.pow(2, mageRank);
        }
        if (mageRank >= 5) this.mageXp = 0;
    }
    @Override public Map<String, Integer> getKnownSpells() { return knownSpells; }
    @Override public void learnSpell(String spellId, int level) { knownSpells.put(spellId, level); }
    @Override public int getSpellLevel(String spellId) { return knownSpells.getOrDefault(spellId, -1); }

    // НОВОЕ: КВЕСТЫ
    @Override public List<Quest> getActiveQuests() { return activeQuests; }
    @Override public void addQuest(Quest quest) { activeQuests.add(quest); }
    @Override public void removeQuest(Quest quest) { activeQuests.remove(quest); }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("level", level); nbt.putInt("xp", xp); nbt.putInt("statPoints", statPoints); nbt.putInt("mana", mana);
        nbt.putInt("strength", strength); nbt.putInt("dexterity", dexterity); nbt.putInt("intelligence", intelligence);
        nbt.putInt("luck", luck); nbt.putInt("vitality", vitality); nbt.putInt("healthRegen", healthRegen);
        nbt.putInt("manaRegen", manaRegen); nbt.putInt("stamina", stamina); nbt.putInt("staminaRegen", staminaRegen);
        nbt.putInt("staminaRegenDelay", staminaRegenDelay); nbt.putInt("copper", copper); nbt.putInt("silver", silver);
        nbt.putInt("gold", gold); nbt.put("buffs", buffs);

        nbt.putInt("mageRank", mageRank); nbt.putInt("mageXp", mageXp);
        CompoundNBT spellsNbt = new CompoundNBT();
        for (Map.Entry<String, Integer> entry : knownSpells.entrySet()) spellsNbt.putInt(entry.getKey(), entry.getValue());
        nbt.put("knownSpells", spellsNbt);

        CompoundNBT profNbt = new CompoundNBT();
        for (Map.Entry<Profession, ProfessionData> e : professions.entrySet()) {
            CompoundNBT d = new CompoundNBT(); e.getValue().writeToNBT(d); profNbt.put(e.getKey().getId(), d);
        }
        nbt.put("professions", profNbt);

        // НОВОЕ: Сериализация квестов
        CompoundNBT questsNbt = new CompoundNBT();
        int i = 0;
        for (Quest q : activeQuests) {
            questsNbt.put("q" + i, q.serializeNBT());
            i++;
        }
        nbt.put("activeQuests", questsNbt);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.level = nbt.contains("level") ? Math.max(1, nbt.getInt("level")) : 1;
        this.xp = nbt.getInt("xp");
        this.statPoints = nbt.getInt("statPoints");
        this.mana = nbt.contains("mana") ? nbt.getInt("mana") : getMaxMana();
        this.strength = nbt.contains("strength") ? Math.max(1, nbt.getInt("strength")) : 1;
        this.dexterity = nbt.contains("dexterity") ? Math.max(1, nbt.getInt("dexterity")) : 1;
        this.intelligence = nbt.contains("intelligence") ? Math.max(1, nbt.getInt("intelligence")) : 1;
        this.luck = nbt.contains("luck") ? Math.max(1, nbt.getInt("luck")) : 1;
        this.vitality = nbt.contains("vitality") ? Math.max(1, nbt.getInt("vitality")) : 1;
        this.healthRegen = nbt.getInt("healthRegen");
        this.manaRegen = nbt.getInt("manaRegen");
        this.stamina = nbt.contains("stamina") ? nbt.getInt("stamina") : 100;
        this.staminaRegen = nbt.getInt("staminaRegen");
        this.staminaRegenDelay = nbt.getInt("staminaRegenDelay");
        this.copper = nbt.getInt("copper"); this.silver = nbt.getInt("silver"); this.gold = nbt.getInt("gold");
        if (nbt.contains("buffs")) this.buffs = nbt.getCompound("buffs");

        this.mageRank = nbt.getInt("mageRank"); this.mageXp = nbt.getInt("mageXp");
        this.knownSpells.clear();
        if (nbt.contains("knownSpells")) {
            CompoundNBT spellsNbt = nbt.getCompound("knownSpells");
            for (String key : spellsNbt.getAllKeys()) knownSpells.put(key, spellsNbt.getInt(key));
        }

        if (nbt.contains("professions")) {
            CompoundNBT profNbt = nbt.getCompound("professions");
            for (Profession p : Profession.values()) if (profNbt.contains(p.getId())) profData(p).readFromNBT(profNbt.getCompound(p.getId()));
        }

        // НОВОЕ: Десериализация квестов
        this.activeQuests.clear();
        if (nbt.contains("activeQuests")) {
            CompoundNBT questsNbt = nbt.getCompound("activeQuests");
            for (String key : questsNbt.getAllKeys()) {
                activeQuests.add(Quest.deserializeNBT(questsNbt.getCompound(key)));
            }
        }
    }
}