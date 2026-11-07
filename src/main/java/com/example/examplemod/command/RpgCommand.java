package com.example.examplemod.command;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.event.PlayerEventHandler;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.item.Rarity;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.spells.Spell;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
                                            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), PlayerEventHandler.buildSyncPacket(data));
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
                                            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), PlayerEventHandler.buildSyncPacket(data));
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
                                            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), PlayerEventHandler.buildSyncPacket(data));
                                        });
                                    }
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("potion")
                        .then(Commands.argument("type", StringArgumentType.string())
                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("rarity", IntegerArgumentType.integer(0, 4))
                                                .executes(context -> {
                                                    ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
                                                    String type = StringArgumentType.getString(context, "type");
                                                    int level = IntegerArgumentType.getInteger(context, "level");
                                                    int rarityInt = IntegerArgumentType.getInteger(context, "rarity");
                                                    if (player != null) {
                                                        ItemStack potion;
                                                        if (type.equalsIgnoreCase("mana")) potion = new ItemStack(ModItems.MANA_POTION.get());
                                                        else potion = new ItemStack(ModItems.HEALTH_POTION.get());
                                                        CompoundNBT nbt = potion.getOrCreateTag();
                                                        nbt.putInt("ItemLevel", level);
                                                        nbt.putInt("RarityLevel", Math.min(4, rarityInt));
                                                        player.inventory.add(potion);
                                                        player.sendMessage(new StringTextComponent("§aЗелье выдано!"), player.getUUID());
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
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
                .then(Commands.literal("npc")
                        .then(Commands.argument("type", StringArgumentType.string())
                                .executes(context -> {
                                    ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
                                    String type = StringArgumentType.getString(context, "type");
                                    if (player != null) {
                                        net.minecraft.entity.merchant.villager.VillagerEntity npc = new net.minecraft.entity.merchant.villager.VillagerEntity(net.minecraft.entity.EntityType.VILLAGER, player.level);
                                        npc.setPos(player.getX() + 2, player.getY(), player.getZ() + 2);
                                        switch (type.toLowerCase()) {
                                            case "guild":
                                                npc.setCustomName(new StringTextComponent("§6Глава Гильдии"));
                                                npc.setVillagerData(npc.getVillagerData().setProfession(net.minecraft.entity.merchant.villager.VillagerProfession.NITWIT));
                                                break;
                                            case "weapon":
                                                npc.setCustomName(new StringTextComponent("§cОружейник"));
                                                npc.setVillagerData(npc.getVillagerData().setProfession(net.minecraft.entity.merchant.villager.VillagerProfession.WEAPONSMITH));
                                                break;
                                            case "armor":
                                                npc.setCustomName(new StringTextComponent("§7Бронник"));
                                                npc.setVillagerData(npc.getVillagerData().setProfession(net.minecraft.entity.merchant.villager.VillagerProfession.ARMORER));
                                                break;
                                            case "mage":
                                                npc.setCustomName(new StringTextComponent("§5Маг"));
                                                npc.setVillagerData(npc.getVillagerData().setProfession(net.minecraft.entity.merchant.villager.VillagerProfession.CLERIC));
                                                break;
                                            default:
                                                player.sendMessage(new StringTextComponent("§cТипы: guild, weapon, armor, mage"), player.getUUID());
                                                return 1;
                                        }
                                        npc.setCustomNameVisible(true);
                                        player.level.addFreshEntity(npc);
                                        player.sendMessage(new StringTextComponent("§aNPC заспавнен!"), player.getUUID());
                                    }
                                    return 1;
                                })
                        )
                )
                // === КОМАНДА ДЛЯ ВЫДАЧИ ТОМОВ ===
                .then(Commands.literal("tome")
                        .then(Commands.argument("spell", StringArgumentType.string())
                                .executes(context -> {
                                    ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
                                    String spellId = StringArgumentType.getString(context, "spell");

                                    if (player != null) {
                                        Spell spell = Spell.byId(spellId.toLowerCase());

                                        ItemStack tome = new ItemStack(ModItems.SPELL_TOME.get());
                                        CompoundNBT nbt = tome.getOrCreateTag();
                                        nbt.putString("SpellId", spell.getId());
                                        nbt.putInt("SpellLevel", spell.getDefaultLevel());

                                        player.inventory.add(tome);
                                        player.sendMessage(new StringTextComponent("§aВыдан Том: " + spell.getName() + " (Уровень " + spell.getDefaultLevel() + ")"), player.getUUID());
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}