package com.example.examplemod.network;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.profession.Profession;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncLevelPacket {
    private final int level, xp, statPoints, strength, dexterity, intelligence, luck, vitality, mana,
            healthRegen, manaRegen, stamina, staminaRegen, copper, silver, gold;
    private final int[] professionLevels;
    private final int[] professionXp;

    private final int mageRank, mageXp;
    private final Map<String, Integer> knownSpells;

    // Старый конструктор (без магии)
    public SyncLevelPacket(int level, int xp, int statPoints, int strength, int dexterity, int intelligence,
                           int luck, int vitality, int mana, int healthRegen, int manaRegen,
                           int stamina, int staminaRegen, int copper, int silver, int gold) {
        this(level, xp, statPoints, strength, dexterity, intelligence, luck, vitality, mana,
                healthRegen, manaRegen, stamina, staminaRegen, copper, silver, gold,
                new int[0], new int[0], 0, 0, new HashMap<>());
    }

    // Конструктор с профессиями
    public SyncLevelPacket(int level, int xp, int statPoints, int strength, int dexterity, int intelligence,
                           int luck, int vitality, int mana, int healthRegen, int manaRegen,
                           int stamina, int staminaRegen, int copper, int silver, int gold,
                           int[] professionLevels, int[] professionXp) {
        this(level, xp, statPoints, strength, dexterity, intelligence, luck, vitality, mana,
                healthRegen, manaRegen, stamina, staminaRegen, copper, silver, gold,
                professionLevels, professionXp, 0, 0, new HashMap<>());
    }

    // Полный конструктор (со всем)
    public SyncLevelPacket(int level, int xp, int statPoints, int strength, int dexterity, int intelligence,
                           int luck, int vitality, int mana, int healthRegen, int manaRegen,
                           int stamina, int staminaRegen, int copper, int silver, int gold,
                           int[] professionLevels, int[] professionXp, int mageRank, int mageXp, Map<String, Integer> knownSpells) {
        this.level = level; this.xp = xp; this.statPoints = statPoints;
        this.strength = strength; this.dexterity = dexterity; this.intelligence = intelligence;
        this.luck = luck; this.vitality = vitality;
        this.mana = mana; this.healthRegen = healthRegen; this.manaRegen = manaRegen;
        this.stamina = stamina; this.staminaRegen = staminaRegen;
        this.copper = copper; this.silver = silver; this.gold = gold;
        this.professionLevels = professionLevels;
        this.professionXp = professionXp;
        this.mageRank = mageRank;
        this.mageXp = mageXp;
        this.knownSpells = knownSpells;
    }

    public static void encode(SyncLevelPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.level); buf.writeInt(msg.xp); buf.writeInt(msg.statPoints);
        buf.writeInt(msg.strength); buf.writeInt(msg.dexterity); buf.writeInt(msg.intelligence);
        buf.writeInt(msg.luck); buf.writeInt(msg.vitality);
        buf.writeInt(msg.mana); buf.writeInt(msg.healthRegen); buf.writeInt(msg.manaRegen);
        buf.writeInt(msg.stamina); buf.writeInt(msg.staminaRegen);
        buf.writeInt(msg.copper); buf.writeInt(msg.silver); buf.writeInt(msg.gold);

        buf.writeVarInt(msg.professionLevels.length);
        for (int v : msg.professionLevels) buf.writeInt(v);
        buf.writeVarInt(msg.professionXp.length);
        for (int v : msg.professionXp) buf.writeInt(v);

        // Магия
        buf.writeInt(msg.mageRank);
        buf.writeInt(msg.mageXp);
        buf.writeInt(msg.knownSpells.size());
        for (Map.Entry<String, Integer> entry : msg.knownSpells.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeInt(entry.getValue());
        }
    }

    public static SyncLevelPacket decode(PacketBuffer buf) {
        int level = buf.readInt(), xp = buf.readInt(), sp = buf.readInt();
        int str = buf.readInt(), dex = buf.readInt(), intel = buf.readInt();
        int luck = buf.readInt(), vit = buf.readInt();
        int mana = buf.readInt(), hr = buf.readInt(), mr = buf.readInt();
        int stam = buf.readInt(), sr = buf.readInt();
        int cop = buf.readInt(), sil = buf.readInt(), gol = buf.readInt();

        int lvlsLen = buf.readVarInt();
        int[] lvls = new int[lvlsLen];
        for (int i = 0; i < lvlsLen; i++) lvls[i] = buf.readInt();

        int xpsLen = buf.readVarInt();
        int[] xps = new int[xpsLen];
        for (int i = 0; i < xpsLen; i++) xps[i] = buf.readInt();

        int mRank = buf.readInt();
        int mXp = buf.readInt();
        int spellSize = buf.readInt();
        Map<String, Integer> spells = new HashMap<>();
        for (int i = 0; i < spellSize; i++) {
            spells.put(buf.readUtf(32767), buf.readInt());
        }

        return new SyncLevelPacket(level, xp, sp, str, dex, intel, luck, vit, mana, hr, mr,
                stam, sr, cop, sil, gol, lvls, xps, mRank, mXp, spells);
    }

    public static void handle(SyncLevelPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = Minecraft.getInstance().player;
            if (player == null) return;
            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                data.setLevel(msg.level); data.setXp(msg.xp); data.setStatPoints(msg.statPoints);
                data.setStrength(msg.strength); data.setDexterity(msg.dexterity);
                data.setIntelligence(msg.intelligence); data.setLuck(msg.luck); data.setVitality(msg.vitality);
                data.setMana(msg.mana); data.setHealthRegen(msg.healthRegen); data.setManaRegen(msg.manaRegen);
                data.setStamina(msg.stamina); data.setStaminaRegen(msg.staminaRegen);
                data.setCopper(msg.copper); data.setSilver(msg.silver); data.setGold(msg.gold);

                Profession[] all = Profession.values();
                if (msg.professionLevels.length == all.length && msg.professionXp.length == all.length) {
                    for (int i = 0; i < all.length; i++) {
                        data.setProfessionLevel(all[i], msg.professionLevels[i]);
                        data.setProfessionXp(all[i], msg.professionXp[i]);
                    }
                }

                // Синхронизация магии
                data.setMageRank(msg.mageRank);
                data.setMageXp(msg.mageXp);
                data.getKnownSpells().clear();
                data.getKnownSpells().putAll(msg.knownSpells);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}