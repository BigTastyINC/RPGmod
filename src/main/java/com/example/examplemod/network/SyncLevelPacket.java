package com.example.examplemod.network;

import com.example.examplemod.capability.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncLevelPacket {
    private final int level, xp, statPoints, strength, dexterity, intelligence, luck, vitality, mana, healthRegen, manaRegen, stamina, copper, silver, gold;

    public SyncLevelPacket(int level, int xp, int statPoints, int strength, int dexterity, int intelligence, int luck, int vitality, int mana, int healthRegen, int manaRegen, int stamina, int copper, int silver, int gold) {
        this.level = level; this.xp = xp; this.statPoints = statPoints;
        this.strength = strength; this.dexterity = dexterity; this.intelligence = intelligence; this.luck = luck; this.vitality = vitality;
        this.mana = mana; this.healthRegen = healthRegen; this.manaRegen = manaRegen;
        this.stamina = stamina;
        this.copper = copper; this.silver = silver; this.gold = gold;
    }

    public static void encode(SyncLevelPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.level); buf.writeInt(msg.xp); buf.writeInt(msg.statPoints);
        buf.writeInt(msg.strength); buf.writeInt(msg.dexterity); buf.writeInt(msg.intelligence); buf.writeInt(msg.luck); buf.writeInt(msg.vitality);
        buf.writeInt(msg.mana); buf.writeInt(msg.healthRegen); buf.writeInt(msg.manaRegen);
        buf.writeInt(msg.stamina);
        buf.writeInt(msg.copper); buf.writeInt(msg.silver); buf.writeInt(msg.gold);
    }

    public static SyncLevelPacket decode(PacketBuffer buf) {
        return new SyncLevelPacket(
                buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readInt(), buf.readInt(),
                buf.readInt(),
                buf.readInt(), buf.readInt(), buf.readInt()
        );
    }

    public static void handle(SyncLevelPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                    data.setLevel(msg.level); data.setXp(msg.xp); data.setStatPoints(msg.statPoints);
                    data.setStrength(msg.strength); data.setDexterity(msg.dexterity); data.setIntelligence(msg.intelligence); data.setLuck(msg.luck); data.setVitality(msg.vitality);
                    data.setMana(msg.mana); data.setHealthRegen(msg.healthRegen); data.setManaRegen(msg.manaRegen);
                    data.setStamina(msg.stamina);
                    data.setCopper(msg.copper); data.setSilver(msg.silver); data.setGold(msg.gold);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}