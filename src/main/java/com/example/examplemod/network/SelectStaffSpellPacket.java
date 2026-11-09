package com.example.examplemod.network;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.item.StaffItem;
import com.example.examplemod.spells.Spell;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SelectStaffSpellPacket {
    private final String spellId;

    public SelectStaffSpellPacket(String spellId) {
        this.spellId = spellId;
    }

    public static void encode(SelectStaffSpellPacket msg, PacketBuffer buf) {
        buf.writeUtf(msg.spellId);
    }

    public static SelectStaffSpellPacket decode(PacketBuffer buf) {
        return new SelectStaffSpellPacket(buf.readUtf(32767));
    }

    public static void handle(SelectStaffSpellPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) return;

            ItemStack staff = player.getMainHandItem();
            if (!(staff.getItem() instanceof StaffItem)) return;

            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                if (data.getSpellLevel(msg.spellId) == -1) {
                    player.displayClientMessage(new StringTextComponent("§cВы не знаете это заклинание!"), true);
                    return;
                }

                CompoundNBT nbt = staff.getOrCreateTag();
                nbt.putString("SelectedSpell", msg.spellId);

                Spell spell = Spell.byId(msg.spellId);
                player.displayClientMessage(new StringTextComponent(
                        "§aВыбрано: §e" + spell.getName()
                ), true);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}