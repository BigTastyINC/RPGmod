package com.example.examplemod.event;

import com.example.examplemod.buffs.RpgBuffs;
import com.example.examplemod.capability.IPlayerLevel;
import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.capability.PlayerLevelProvider;
import com.example.examplemod.item.ItemStatHelper;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.item.Rarity;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.SyncLevelPacket;
import com.example.examplemod.profession.Profession;
import com.example.examplemod.profession.ProfessionData;
import com.example.examplemod.profession.RarityRoller;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "examplemod")
public class PlayerEventHandler {

    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("d1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private static final UUID DAMAGE_MODIFIER_ID = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("b1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            if (!event.getCapabilities().containsKey(new ResourceLocation("examplemod:player_level"))) {
                event.addCapability(new ResourceLocation("examplemod:player_level"), new PlayerLevelProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        PlayerEntity player = event.getPlayer();
        if (!player.level.isClientSide) {
            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                if (data.getStamina() >= 15) {
                    data.setStamina(data.getStamina() - 15);
                    data.setStaminaRegenDelay(40);
                    syncData(player, data);
                } else {
                    event.setCanceled(true);
                    player.displayClientMessage(new StringTextComponent("§cНедостаточно стамины!"), true);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getPlayer().level.isClientSide) return;
        PlayerEntity player = event.getPlayer();
        ItemStack craftedItem = event.getCrafting();

        if (!ItemStatHelper.isGear(craftedItem) && !ItemStatHelper.isPotion(craftedItem)) return;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {

            // === ЛОГИКА ДЛЯ ЗЕЛИЙ (АЛХИМИК) ===
            if (ItemStatHelper.isPotion(craftedItem)) {
                Profession prof = Profession.ALCHEMIST;
                int xpGain = 50; // Опыт за зелье

                boolean leveledUp = data.addProfessionXp(prof, xpGain);
                int profLevel = data.getProfessionLevel(prof);
                Rarity rarity = RarityRoller.roll(profLevel);

                ItemStatHelper.applyPotionRarity(craftedItem, data.getLevel(), rarity);

                player.displayClientMessage(new StringTextComponent(
                        "§aСоздано зелье: " + rarity.getColor() + "[" + rarity.getName() + "]"
                                + " §7ур. " + data.getLevel()
                                + " §8| §7Алхимик: §e" + profLevel
                ), false);

                if (leveledUp) {
                    player.displayClientMessage(new StringTextComponent(
                            "§6⬆ Профессия повышена: §eАлхимик §7→ §e" + profLevel
                    ), false);
                }

                syncData(player, data);
                return;
            }

            // === ЛОГИКА ДЛЯ ОРУЖИЯ/БРОНИ ===
            boolean isWeapon = ItemStatHelper.isWeapon(craftedItem);
            Profession prof = isWeapon ? Profession.WEAPONSMITH : Profession.ARMORSMITH;
            int xpGain = isWeapon ? 100 : 50;

            boolean leveledUp = data.addProfessionXp(prof, xpGain);
            int profLevel = data.getProfessionLevel(prof);
            Rarity rarity = RarityRoller.roll(profLevel);

            ItemStatHelper.generateStatsWithRarity(craftedItem, data.getLevel(), rarity);

            player.displayClientMessage(new StringTextComponent(
                    "§aСоздано: " + rarity.getColor() + "[" + rarity.getName() + "]"
                            + " §7ур. " + data.getLevel()
                            + " §8| §7" + prof.getDisplayName() + ": §e" + profLevel
                            + (profLevel >= ProfessionData.MAX_LEVEL ? " §6(МАКС)" : "")
            ), false);

            if (leveledUp) {
                player.displayClientMessage(new StringTextComponent(
                        "§6⬆ Профессия повышена: §e" + prof.getDisplayName() + " §7→ §e" + profLevel
                ), false);
            }

            syncData(player, data);
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) return;
        PlayerEntity player = event.player;

        if (player.getFoodData().getFoodLevel() != 19) {
            player.getFoodData().setFoodLevel(19);
            player.getFoodData().setSaturation(0);
        }

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            boolean changed = false;

            if (player.tickCount % 20 == 0) {
                scanInventory(player, data);

                if (data.getStaminaRegenDelay() > 0) {
                    data.setStaminaRegenDelay(data.getStaminaRegenDelay() - 20);
                    if (data.getStaminaRegenDelay() < 0) data.setStaminaRegenDelay(0);
                } else if (data.getStamina() < data.getMaxStamina()) {
                    int regenAmount = 20 + data.getStaminaRegen() * 2;
                    data.setStamina(data.getStamina() + regenAmount);
                    changed = true;
                }

                int manaRegenAmount = 1 + data.getManaRegen();
                if (data.hasBuff(RpgBuffs.MANA_REGEN)) {
                    long end = data.getBuffEnd(RpgBuffs.MANA_REGEN);
                    if (player.level.getGameTime() < end) {
                        manaRegenAmount += (int) Math.ceil(data.getBuffPower(RpgBuffs.MANA_REGEN));
                    } else {
                        data.clearBuff(RpgBuffs.MANA_REGEN);
                    }
                }
                if (data.getMana() < data.getMaxMana()) {
                    data.setMana(data.getMana() + manaRegenAmount);
                    changed = true;
                }

                float healthRegenAmount = data.getHealthRegen() / 3.0f;
                if (data.hasBuff(RpgBuffs.HEALTH_REGEN)) {
                    long end = data.getBuffEnd(RpgBuffs.HEALTH_REGEN);
                    if (player.level.getGameTime() < end) {
                        healthRegenAmount += data.getBuffPower(RpgBuffs.HEALTH_REGEN);
                    } else {
                        data.clearBuff(RpgBuffs.HEALTH_REGEN);
                    }
                }
                if (healthRegenAmount > 0 && player.getHealth() < player.getMaxHealth()) {
                    player.heal(healthRegenAmount);
                    changed = true;
                }

                applyStats(player, data);
            }

            if (changed) syncData(player, data);
        });
    }

    private static void scanInventory(PlayerEntity player, IPlayerLevel data) {
        if (!(player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity sp = (ServerPlayerEntity) player;
        boolean modified = false;

        for (int i = 0; i < sp.inventory.getContainerSize(); i++) {
            ItemStack stack = sp.inventory.getItem(i);

            if (ItemStatHelper.isGear(stack) && (!stack.hasTag() || !stack.getTag().contains("ItemLevel"))) {
                boolean isWeapon = ItemStatHelper.isWeapon(stack);
                Profession prof = isWeapon ? Profession.WEAPONSMITH : Profession.ARMORSMITH;
                int profLevel = data.getProfessionLevel(prof);
                Rarity rarity = RarityRoller.roll(profLevel);
                ItemStatHelper.generateStatsWithRarity(stack, data.getLevel(), rarity);
                modified = true;
            }

            if (ItemStatHelper.isPotion(stack) && (!stack.hasTag() || !stack.getTag().contains("ItemLevel"))) {
                int profLevel = data.getProfessionLevel(Profession.ALCHEMIST);
                Rarity rarity = RarityRoller.roll(profLevel);
                ItemStatHelper.applyPotionRarity(stack, data.getLevel(), rarity);
                modified = true;
            }

            if (FoodBuffHandler.isPreparedFood(stack.getItem())
                    && (!stack.hasTag() || !stack.getTag().contains("RarityLevel"))) {
                FoodBuffHandler.assignRarityIfFood(stack);
                modified = true;
            }
        }

        if (modified) sp.containerMenu.broadcastChanges();
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(oldData -> {
                event.getPlayer().getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(newData -> {
                    CompoundNBT saved = oldData.serializeNBT();
                    newData.deserializeNBT(saved);
                    applyStats(event.getPlayer(), newData);
                });
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();
        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            applyStats(player, data);
            syncData(player, data);
            giveBook(player);
        });
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        PlayerEntity player = event.getPlayer();
        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            applyStats(player, data);
            syncData(player, data);
            giveBook(player);
        });
    }

    private static void giveBook(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity sp = (ServerPlayerEntity) player;
            boolean hasBook = false;
            for (int i = 0; i < sp.inventory.getContainerSize(); i++) {
                if (sp.inventory.getItem(i).getItem() == ModItems.STATS_BOOK.get()) { hasBook = true; break; }
            }
            if (!hasBook) sp.inventory.add(new ItemStack(ModItems.STATS_BOOK.get()));
        }
    }

    public static void applyStats(PlayerEntity player, IPlayerLevel data) {
        if (player == null) return;
        int totalStr = data.getStrength();
        int totalDex = data.getDexterity();
        int totalVit = data.getVitality();

        ItemStack heldItem = player.getMainHandItem();
        if (ItemStatHelper.isWeapon(heldItem) && heldItem.hasTag()) {
            CompoundNBT nbt = heldItem.getTag();
            if (nbt != null) {
                totalStr += nbt.getInt("StatStrength");
                totalDex += nbt.getInt("StatDexterity");
            }
        }
        for (ItemStack armor : player.inventory.armor) {
            if (ItemStatHelper.isArmor(armor) && armor.hasTag()) {
                CompoundNBT nbt = armor.getTag();
                if (nbt != null) {
                    totalStr += nbt.getInt("StatStrength");
                    totalDex += nbt.getInt("StatDexterity");
                    totalVit += nbt.getInt("StatVitality");
                }
            }
        }

        ModifiableAttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            AttributeModifier existing = maxHealth.getModifier(HEALTH_MODIFIER_ID);
            if (existing != null) maxHealth.removeModifier(existing);
            if (totalVit > 1) {
                double bonus = (totalVit - 1) * 2.0;
                maxHealth.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_ID, "rpg_health", bonus, AttributeModifier.Operation.ADDITION));
            }
        }

        ModifiableAttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            AttributeModifier existing = attackDamage.getModifier(DAMAGE_MODIFIER_ID);
            if (existing != null) attackDamage.removeModifier(existing);
            if (totalStr > 1) {
                double bonus = (totalStr - 1) * 0.5;
                attackDamage.addPermanentModifier(new AttributeModifier(DAMAGE_MODIFIER_ID, "rpg_strength", bonus, AttributeModifier.Operation.ADDITION));
            }
        }

        ModifiableAttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            AttributeModifier existing = attackSpeed.getModifier(ATTACK_SPEED_MODIFIER_ID);
            if (existing != null) attackSpeed.removeModifier(existing);
            if (totalDex > 1) {
                double bonus = (totalDex - 1) * 0.1;
                attackSpeed.addPermanentModifier(new AttributeModifier(ATTACK_SPEED_MODIFIER_ID, "rpg_dexterity", bonus, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    public static SyncLevelPacket buildSyncPacket(IPlayerLevel data) {
        Profession[] all = Profession.values();
        int[] levels = new int[all.length];
        int[] xps = new int[all.length];
        for (Profession p : all) {
            levels[p.ordinal()] = data.getProfessionLevel(p);
            xps[p.ordinal()] = data.getProfessionXp(p);
        }
        return new SyncLevelPacket(
                data.getLevel(), data.getXp(), data.getStatPoints(),
                data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(), data.getVitality(),
                data.getMana(), data.getHealthRegen(), data.getManaRegen(),
                data.getStamina(), data.getStaminaRegen(),
                data.getCopper(), data.getSilver(), data.getGold(),
                levels, xps
        );
    }

    public static void syncData(PlayerEntity player, IPlayerLevel data) {
        if (player instanceof ServerPlayerEntity) {
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                    buildSyncPacket(data)
            );
        }
    }
}