package com.example.examplemod.command;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.event.PlayerEventHandler;
import com.example.examplemod.item.Rarity;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.SyncLevelPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;

public class RpgCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("rpg")
                .then(Commands.literal("level")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                    if (player != null) {
                                        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                                            data.addXp(amount);
                                            PlayerEventHandler.applyStats(player, data);
                                            player.sendMessage(new StringTextComponent("Добавлено " + amount + " опыта!"), player.getUUID());
                                            // В пакет добавлена data.getStamina()
                                            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncLevelPacket(data.getLevel(), data.getXp(), data.getStatPoints(), data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(), data.getVitality(), data.getMana(), data.getHealthRegen(), data.getManaRegen(), data.getStamina(), data.getCopper(), data.getSilver(), data.getGold()));
                                        });
                                    }
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("addpoints")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                    if (player != null) {
                                        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                                            data.addStatPoints(amount);
                                            player.sendMessage(new StringTextComponent("§aДобавлено " + amount + " очков!"), player.getUUID());
                                            // В пакет добавлена data.getStamina()
                                            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncLevelPacket(data.getLevel(), data.getXp(), data.getStatPoints(), data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(), data.getVitality(), data.getMana(), data.getHealthRegen(), data.getManaRegen(), data.getStamina(), data.getCopper(), data.getSilver(), data.getGold()));
                                        });
                                    }
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("addcoins")
                        .then(Commands.argument("copperAmount", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
                                    int amount = IntegerArgumentType.getInteger(context, "copperAmount");
                                    if (player != null) {
                                        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                                            data.addCoins(amount);
                                            player.sendMessage(new StringTextComponent("§6Добавлено " + amount + " меди!"), player.getUUID());
                                            // В пакет добавлена data.getStamina()
                                            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncLevelPacket(data.getLevel(), data.getXp(), data.getStatPoints(), data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(), data.getVitality(), data.getMana(), data.getHealthRegen(), data.getManaRegen(), data.getStamina(), data.getCopper(), data.getSilver(), data.getGold()));
                                        });
                                    }
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("weapon")
                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                .then(Commands.argument("rarity", IntegerArgumentType.integer(0, 4))
                                        .executes(context -> {
                                            ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
                                            int level = IntegerArgumentType.getInteger(context, "level");
                                            int rarityInt = IntegerArgumentType.getInteger(context, "rarity");
                                            if (player != null) {
                                                ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
                                                CompoundNBT nbt = sword.getOrCreateTag();
                                                nbt.putInt("ItemLevel", level);
                                                nbt.putInt("RarityLevel", rarityInt);
                                                int baseStat = (int) (level * 0.5 * Rarity.values()[rarityInt].getMultiplier());
                                                nbt.putInt("StatStrength", baseStat);
                                                nbt.putInt("StatDexterity", baseStat);
                                                player.inventory.add(sword);
                                                player.sendMessage(new StringTextComponent("§6Выдан: Алмазный меч"), player.getUUID());
                                            }
                                            return 1;
                                        })
                                )
                        )
                )
                .then(Commands.literal("armor")
                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                .then(Commands.argument("rarity", IntegerArgumentType.integer(0, 4))
                                        .executes(context -> {
                                            ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
                                            int level = IntegerArgumentType.getInteger(context, "level");
                                            int rarityInt = IntegerArgumentType.getInteger(context, "rarity");
                                            if (player != null) {
                                                ItemStack armor = new ItemStack(Items.DIAMOND_CHESTPLATE);
                                                CompoundNBT nbt = armor.getOrCreateTag();
                                                nbt.putInt("ItemLevel", level);
                                                nbt.putInt("RarityLevel", rarityInt);
                                                int baseStat = (int) (level * 0.5 * Rarity.values()[rarityInt].getMultiplier());
                                                nbt.putInt("StatStrength", baseStat / 2);
                                                nbt.putInt("StatDexterity", baseStat / 2);
                                                nbt.putInt("StatVitality", baseStat);
                                                player.inventory.add(armor);
                                                player.sendMessage(new StringTextComponent("§6Выдан: Алмазный нагрудник"), player.getUUID());
                                            }
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}