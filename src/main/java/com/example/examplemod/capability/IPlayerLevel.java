package com.example.examplemod.capability;

import com.example.examplemod.profession.Profession;
import com.example.examplemod.quest.Quest;
import net.minecraft.nbt.CompoundNBT;

import java.util.List;
import java.util.Map;

public interface IPlayerLevel {
    int getLevel(); void setLevel(int level); int getXp(); void setXp(int xp); void addXp(int amount);
    int getStatPoints(); void setStatPoints(int points); void addStatPoints(int points);
    int getStrength(); void setStrength(int value); int getDexterity(); void setDexterity(int value);
    int getIntelligence(); void setIntelligence(int value); int getLuck(); void setLuck(int value);
    int getVitality(); void setVitality(int value);
    int getHealthRegen(); void setHealthRegen(int value); int getManaRegen(); void setManaRegen(int value);
    int getStamina(); void setStamina(int stamina); int getStaminaRegen(); void setStaminaRegen(int value); int getStaminaRegenDelay(); void setStaminaRegenDelay(int delay);
    default int getMaxStamina() { return 100 + (getStrength() - 1) * 3; }
    int getMana(); void setMana(int mana);
    default int getMaxMana() { return 100 + (getIntelligence() - 1) * 10; }
    int getCopper(); void setCopper(int copper); int getSilver(); void setSilver(int silver); int getGold(); void setGold(int gold); void addCoins(int copperAmount);
    void setBuff(int buffId, long endTime, float power); long getBuffEnd(int buffId); float getBuffPower(int buffId); boolean hasBuff(int buffId); void clearBuff(int buffId); CompoundNBT getAllBuffs();
    int getProfessionLevel(Profession p); int getProfessionXp(Profession p); void setProfessionLevel(Profession p, int level); void setProfessionXp(Profession p, int xp); boolean addProfessionXp(Profession p, int amount);

    // МАГИЯ
    int getMageRank(); void setMageRank(int rank); int getMageXp(); void setMageXp(int xp); void addMageXp(int amount);
    Map<String, Integer> getKnownSpells(); void learnSpell(String spellId, int level); int getSpellLevel(String spellId);

    // НОВОЕ: КВЕСТЫ
    List<Quest> getActiveQuests();
    void addQuest(Quest quest);
    void removeQuest(Quest quest);

    CompoundNBT serializeNBT();
    void deserializeNBT(CompoundNBT nbt);
}