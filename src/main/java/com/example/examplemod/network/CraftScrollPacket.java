package com.example.examplemod.network;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.spells.Spell;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class CraftScrollPacket {
    private final String spellId;

    public CraftScrollPacket(String spellId) {
        this.spellId = spellId;
    }

    public static void encode(CraftScrollPacket msg, PacketBuffer buf) {
        buf.writeUtf(msg.spellId);
    }

    public static CraftScrollPacket decode(PacketBuffer buf) {
        return new CraftScrollPacket(buf.readUtf(32767));
    }

    public static void handle(CraftScrollPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                Spell spell = Spell.byId(msg.spellId);
                int spellLevel = data.getSpellLevel(msg.spellId);

                if (spellLevel == -1) return;
                if (data.getMageRank() < spellLevel) {
                    player.sendMessage(new StringTextComponent("§cВаш ранг магии слишком низок!"), player.getUUID());
                    return;
                }

                // НОВОЕ: Крафт стоит в 2 раза больше маны с учетом ранга
                int manaCost = spell.getManaCost(data.getMageRank()) * 2;

                if (data.getMana() < manaCost) {
                    player.sendMessage(new StringTextComponent("§cНедостаточно маны! Требуется: " + manaCost), player.getUUID());
                    return;
                }

                data.setMana(data.getMana() - manaCost);

                ItemStack scroll = new ItemStack(ModItems.SPELL_SCROLL.get());
                CompoundNBT nbt = scroll.getOrCreateTag();
                nbt.putString("SpellId", spell.getId());
                nbt.putInt("SpellLevel", spellLevel);
                nbt.putInt("RarityLevel", Math.min(4, data.getMageRank()));

                player.inventory.add(scroll);
                player.sendMessage(new StringTextComponent("§aСвиток создан! §7(-" + manaCost + " маны)"), player.getUUID());

                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                        new SyncLevelPacket(data.getLevel(), data.getXp(), data.getStatPoints(),
                                data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(), data.getVitality(),
                                data.getMana(), data.getHealthRegen(), data.getManaRegen(),
                                data.getStamina(), data.getStaminaRegen(),
                                data.getCopper(), data.getSilver(), data.getGold()));
            });
        });
        ctx.get().setPacketHandled(true);
    }
}