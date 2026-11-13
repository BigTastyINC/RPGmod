package com.example.examplemod.event;

import com.example.examplemod.buffs.RpgBuffs;
import com.example.examplemod.capability.IPlayerLevel;
import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.capability.PlayerLevelProvider;
import com.example.examplemod.item.ItemAffix;
import com.example.examplemod.item.ItemStatHelper;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.item.Rarity;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.SyncLevelPacket;
import com.example.examplemod.profession.Profession;
import com.example.examplemod.profession.RarityRoller;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "examplemod")
public class PlayerEventHandler {

    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("d1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private static final UUID DAMAGE_MODIFIER_ID = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("b1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private static final UUID KNOCKBACK_MODIFIER_ID = UUID.fromString("c1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private static final UUID SWIFTNESS_MODIFIER_ID = UUID.fromString("e1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private static final UUID BLOODLUST_HP_MODIFIER_ID = UUID.fromString("f1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private static final UUID AGILITY_MODIFIER_ID = UUID.fromString("a2b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");

    public static int getProfessionXpWithBonus(PlayerEntity player, IPlayerLevel data, int baseXp) {
        if (data.hasBuff(RpgBuffs.PROFESSION_LUCK) && player.level.getGameTime() < data.getBuffEnd(RpgBuffs.PROFESSION_LUCK)) {
            return (int)(baseXp * 1.5f);
        }
        return baseXp;
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            if (!event.getCapabilities().containsKey(new ResourceLocation("examplemod:player_level"))) {
                event.addCapability(new ResourceLocation("examplemod:player_level"), new PlayerLevelProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntityItem().getItem();
        if (stack.getItem() == ModItems.STATS_BOOK.get()) {
            event.setCanceled(true);
            PlayerEntity player = event.getPlayer();
            if (player instanceof ServerPlayerEntity) {
                ServerPlayerEntity sp = (ServerPlayerEntity) player;
                sp.inventory.add(stack.copy());
                sp.containerMenu.broadcastChanges();
                sp.displayClientMessage(new StringTextComponent("§cЭтот предмет нельзя выбросить!"), true);
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

            // === ЗЕЛЬЯ ===
            if (ItemStatHelper.isPotion(craftedItem)) {
                Profession prof = Profession.ALCHEMIST;
                int xpGain = getProfessionXpWithBonus(player, data, 50);
                boolean leveledUp = data.addProfessionXp(prof, xpGain);
                int profLevel = data.getProfessionLevel(prof);
                Rarity rarity = RarityRoller.roll(profLevel, data.getLuck());

                ItemStatHelper.applyPotionRarity(craftedItem, data.getLevel(), rarity);

                player.displayClientMessage(new StringTextComponent(
                        "§aСоздано зелье: " + rarity.getColor() + "[" + rarity.getName() + "]"
                                + " §7ур. " + data.getLevel()
                                + " §8| §7Алхимик: §e" + profLevel + " §7(+" + xpGain + " XP)"
                ), false);

                if (leveledUp) {
                    player.displayClientMessage(new StringTextComponent(
                            "§6⬆ Профессия повышена: §eАлхимик §7→ §e" + profLevel
                    ), false);
                }
                player.containerMenu.broadcastChanges();
                syncData(player, data);
                return;
            }

            // === ПОСОХИ ===
            if (ItemStatHelper.isStaff(craftedItem)) {
                Profession prof = Profession.ALCHEMIST;
                int xpGain = getProfessionXpWithBonus(player, data, 100);
                boolean leveledUp = data.addProfessionXp(prof, xpGain);
                int profLevel = data.getProfessionLevel(prof);
                Rarity rarity = RarityRoller.roll(profLevel, data.getLuck());

                ItemStatHelper.generateStatsWithRarity(craftedItem, data.getLevel(), rarity);
                int staffEnergy = calculateStaffEnergyFromCrafting(event.getInventory(), craftedItem);
                CompoundNBT staffNbt = craftedItem.getOrCreateTag();
                staffNbt.putInt("StaffEnergy", staffEnergy);
                craftedItem.setDamageValue(0);

                player.displayClientMessage(new StringTextComponent(
                        "§aСоздан посох: " + rarity.getColor() + "[" + rarity.getName() + "]"
                                + " §7ур. " + data.getLevel()
                                + " §8| §7Алхимик: §e" + profLevel
                                + " §8| §dЭнергия: §e" + staffEnergy
                ), false);

                if (leveledUp) {
                    player.displayClientMessage(new StringTextComponent(
                            "§6⬆ Профессия повышена: §eАлхимик §7→ §e" + profLevel
                    ), false);
                }
                player.containerMenu.broadcastChanges();
                syncData(player, data);
                return;
            }

            // === БИЖУТЕРИЯ ===
            if (ItemStatHelper.isJewelry(craftedItem)) {
                Profession prof = Profession.ALCHEMIST;
                int xpGain = getProfessionXpWithBonus(player, data, 75);
                boolean leveledUp = data.addProfessionXp(prof, xpGain);
                int profLevel = data.getProfessionLevel(prof);
                Rarity rarity = RarityRoller.roll(profLevel, data.getLuck());

                ItemStatHelper.generateStatsWithRarity(craftedItem, data.getLevel(), rarity);

                player.displayClientMessage(new StringTextComponent(
                        "§aСоздана бижутерия: " + rarity.getColor() + "[" + rarity.getName() + "]"
                                + " §7ур. " + data.getLevel()
                                + " §8| §7Алхимик: §e" + profLevel
                ), false);

                if (leveledUp) {
                    player.displayClientMessage(new StringTextComponent(
                            "§6⬆ Профессия повышена: §eАлхимик §7→ §e" + profLevel
                    ), false);
                }
                player.containerMenu.broadcastChanges();
                syncData(player, data);
                return;
            }

            // === ОРУЖИЕ И БРОНЯ (КУЗНЕЦ) ===
            boolean isWeapon = ItemStatHelper.isWeapon(craftedItem);
            Profession prof = Profession.SMITH; // ИЗМЕНЕНО
            int xpGain = getProfessionXpWithBonus(player, data, isWeapon ? 100 : 50);
            boolean leveledUp = data.addProfessionXp(prof, xpGain);
            int profLevel = data.getProfessionLevel(prof);
            Rarity rarity = RarityRoller.roll(profLevel, data.getLuck());

            ItemStatHelper.generateStatsWithRarity(craftedItem, data.getLevel(), rarity);

            player.displayClientMessage(new StringTextComponent(
                    "§aСоздано: " + rarity.getColor() + "[" + rarity.getName() + "]"
                            + " §7ур. " + data.getLevel()
                            + " §8| §7" + prof.getDisplayName() + ": §e" + profLevel
                            + (profLevel >= prof.getMaxLevel() ? " §6(МАКС)" : "")
                            + " §7(+" + xpGain + " XP)"
            ), false);

            if (leveledUp) {
                player.displayClientMessage(new StringTextComponent(
                        "§6⬆ Профессия повышена: §e" + prof.getDisplayName() + " §7→ §e" + profLevel
                ), false);
            }
            player.containerMenu.broadcastChanges();
            syncData(player, data);
        });
    }

    private static int calculateStaffEnergyFromCrafting(IInventory inv, ItemStack craftedItem) {
        if (inv != null) {
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (s.isEmpty()) continue;
                if (s.getItem() == ModItems.SMALL_MAGIC_CRYSTAL.get() || s.getItem() == ModItems.MEDIUM_MAGIC_CRYSTAL.get() || s.getItem() == ModItems.LARGE_MAGIC_CRYSTAL.get()) {
                    return getCrystalEnergy(s);
                }
            }
        }
        if (craftedItem.getItem() == ModItems.SMALL_STAFF.get()) return 500;
        if (craftedItem.getItem() == ModItems.MEDIUM_STAFF.get()) return 1000;
        return 2000;
    }

    private static int getCrystalEnergy(ItemStack crystal) {
        CompoundNBT cn = crystal.getTag();
        int cLevel = (cn != null && cn.contains("ItemLevel")) ? Math.max(1, cn.getInt("ItemLevel")) : 1;
        int cRarity = (cn != null && cn.contains("RarityLevel")) ? Math.min(4, cn.getInt("RarityLevel")) : 0;
        return (int) (cLevel * 10 * Rarity.values()[cRarity].getMultiplier());
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
            if (player.tickCount % 100 == 0) syncData(player, data);

            if (player.tickCount % 20 == 0) {
                scanInventory(player, data);

                if (data.getStaminaRegenDelay() > 0) {
                    data.setStaminaRegenDelay(data.getStaminaRegenDelay() - 20);
                    if (data.getStaminaRegenDelay() < 0) data.setStaminaRegenDelay(0);
                } else if (data.getStamina() < data.getMaxStamina()) {
                    data.setStamina(data.getStamina() + 20 + data.getStaminaRegen() * 2);
                    changed = true;
                }

                int manaRegenAmount = 1 + data.getManaRegen();
                if (data.hasBuff(RpgBuffs.MANA_REGEN)) {
                    long end = data.getBuffEnd(RpgBuffs.MANA_REGEN);
                    if (player.level.getGameTime() < end) manaRegenAmount += (int) Math.ceil(data.getBuffPower(RpgBuffs.MANA_REGEN));
                    else data.clearBuff(RpgBuffs.MANA_REGEN);
                }
                ItemStack mainHand = player.getMainHandItem();
                if (ItemStatHelper.isStaff(mainHand) && ItemStatHelper.hasAffix(mainHand, ItemAffix.MANA_FLOW)) {
                    manaRegenAmount = (int)(manaRegenAmount * 1.30f);
                }
                if (data.getMana() < data.getMaxMana()) {
                    data.setMana(data.getMana() + manaRegenAmount);
                    changed = true;
                }

                float healthRegenAmount = data.getHealthRegen() / 3.0f;
                if (data.hasBuff(RpgBuffs.HEALTH_REGEN)) {
                    long end = data.getBuffEnd(RpgBuffs.HEALTH_REGEN);
                    if (player.level.getGameTime() < end) healthRegenAmount += data.getBuffPower(RpgBuffs.HEALTH_REGEN);
                    else data.clearBuff(RpgBuffs.HEALTH_REGEN);
                }

                Optional<top.theillusivec4.curios.api.type.capability.ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosHelper().getCuriosHandler(player).resolve();
                if (curiosHandler.isPresent()) {
                    Map<String, ICurioStacksHandler> curioMap = curiosHandler.get().getCurios();
                    for (ICurioStacksHandler stacksHandler : curioMap.values()) {
                        IDynamicStackHandler stacks = stacksHandler.getStacks();
                        for (int i = 0; i < stacks.getSlots(); i++) {
                            ItemStack stack = stacks.getStackInSlot(i);
                            if (!stack.isEmpty() && ItemStatHelper.hasAffix(stack, ItemAffix.REGEN_AURA)) healthRegenAmount += 1.0f;
                        }
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

            if (ItemStatHelper.isStaff(stack)) {
                CompoundNBT nbt = stack.getTag();
                boolean needsEnergyFix = (nbt == null || !nbt.contains("StaffEnergy")) || (nbt.getInt("StaffEnergy") == 100);
                if (needsEnergyFix) {
                    int energy = stack.getItem() == ModItems.SMALL_STAFF.get() ? 500 : stack.getItem() == ModItems.MEDIUM_STAFF.get() ? 1000 : 2000;
                    stack.getOrCreateTag().putInt("StaffEnergy", energy);
                    stack.setDamageValue(0);
                    modified = true;
                }
                if (nbt == null || !nbt.contains("ItemLevel")) {
                    ItemStatHelper.generateStatsWithRarity(stack, Math.max(1, data.getLevel()), RarityRoller.roll(data.getProfessionLevel(Profession.ALCHEMIST), data.getLuck()));
                    modified = true;
                }
                continue;
            }

            if (ItemStatHelper.isGear(stack) && (!stack.hasTag() || !stack.getTag().contains("ItemLevel"))) {
                ItemStatHelper.generateStatsWithRarity(stack, Math.max(1, data.getLevel()), RarityRoller.roll(data.getProfessionLevel(Profession.SMITH), data.getLuck())); // ИЗМЕНЕНО
                modified = true;
            }

            if (ItemStatHelper.isPotion(stack) && (!stack.hasTag() || !stack.getTag().contains("ItemLevel"))) {
                ItemStatHelper.applyPotionRarity(stack, Math.max(1, data.getLevel()), RarityRoller.roll(data.getProfessionLevel(Profession.ALCHEMIST), data.getLuck()));
                modified = true;
            }

            if (FoodBuffHandler.isPreparedFood(stack.getItem()) && (!stack.hasTag() || !stack.getTag().contains("RarityLevel"))) {
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
                    newData.deserializeNBT(oldData.serializeNBT());
                    applyStats(event.getPlayer(), newData);
                });
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        event.getPlayer().getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            applyStats(event.getPlayer(), data);
            syncData(event.getPlayer(), data);
        });
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        event.getPlayer().getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            applyStats(event.getPlayer(), data);
            syncData(event.getPlayer(), data);
        });
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

        boolean hasUnbreakable = false;
        boolean hasSwiftness = false;
        boolean hasBloodlust = false;
        boolean hasAgility = false;
        int staminaBonusFromArmor = 0;
        int jInt = 0;

        for (ItemStack armor : player.inventory.armor) {
            if (ItemStatHelper.isArmor(armor) && armor.hasTag()) {
                CompoundNBT nbt = armor.getTag();
                if (nbt != null) {
                    totalStr += nbt.getInt("StatStrength");
                    totalDex += nbt.getInt("StatDexterity");
                    totalVit += nbt.getInt("StatVitality");
                }
            }
            if (ItemStatHelper.hasAffix(armor, ItemAffix.UNBREAKABLE)) hasUnbreakable = true;
            if (ItemStatHelper.hasAffix(armor, ItemAffix.SWIFTNESS)) hasSwiftness = true;
            if (ItemStatHelper.hasAffix(armor, ItemAffix.RESILIENCE)) staminaBonusFromArmor += 15;
        }

        if (ItemStatHelper.isWeapon(heldItem) && ItemStatHelper.hasAffix(heldItem, ItemAffix.BLOODLUST)) {
            hasBloodlust = true;
        }

        int manaBonusFromStaff = 0;
        if (ItemStatHelper.isStaff(heldItem) && ItemStatHelper.hasAffix(heldItem, ItemAffix.MANA_RESERVE)) {
            int baseMaxMana = 100 + (data.getIntelligence() - 1) * 10;
            manaBonusFromStaff = (int)(baseMaxMana * 0.15f);
        }

        Optional<top.theillusivec4.curios.api.type.capability.ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosHelper().getCuriosHandler(player).resolve();
        if (curiosHandler.isPresent()) {
            Map<String, ICurioStacksHandler> curioMap = curiosHandler.get().getCurios();
            for (ICurioStacksHandler stacksHandler : curioMap.values()) {
                IDynamicStackHandler stacks = stacksHandler.getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (stack.isEmpty()) continue;
                    CompoundNBT nbt = stack.getTag();
                    if (nbt != null) {
                        totalStr += nbt.getInt("StatStrength");
                        totalDex += nbt.getInt("StatDexterity");
                        jInt += nbt.getInt("StatIntelligence");
                        totalVit += nbt.getInt("StatVitality");
                    }
                    if (ItemStatHelper.hasAffix(stack, ItemAffix.AGILITY)) hasAgility = true;
                }
            }
        }

        data.setStaminaBonus(staminaBonusFromArmor);
        data.setManaBonus(manaBonusFromStaff + (jInt * 10));

        ModifiableAttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            AttributeModifier existing = maxHealth.getModifier(HEALTH_MODIFIER_ID);
            if (existing != null) maxHealth.removeModifier(existing);
            double bonus = 0;
            if (totalVit > 1) bonus += (totalVit - 1) * 2.0;
            if (bonus > 0) maxHealth.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_ID, "rpg_health", bonus, AttributeModifier.Operation.ADDITION));

            AttributeModifier bloodlustMod = maxHealth.getModifier(BLOODLUST_HP_MODIFIER_ID);
            if (bloodlustMod != null) maxHealth.removeModifier(bloodlustMod);
            if (hasBloodlust) maxHealth.addPermanentModifier(new AttributeModifier(BLOODLUST_HP_MODIFIER_ID, "affix_bloodlust", -0.10, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        ModifiableAttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            AttributeModifier existing = attackDamage.getModifier(DAMAGE_MODIFIER_ID);
            if (existing != null) attackDamage.removeModifier(existing);
            if (totalStr > 1) attackDamage.addPermanentModifier(new AttributeModifier(DAMAGE_MODIFIER_ID, "rpg_strength", (totalStr - 1) * 0.5, AttributeModifier.Operation.ADDITION));
        }

        ModifiableAttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            AttributeModifier existing = attackSpeed.getModifier(ATTACK_SPEED_MODIFIER_ID);
            if (existing != null) attackSpeed.removeModifier(existing);
            if (totalDex > 1) attackSpeed.addPermanentModifier(new AttributeModifier(ATTACK_SPEED_MODIFIER_ID, "rpg_dexterity", (totalDex - 1) * 0.1, AttributeModifier.Operation.ADDITION));
        }

        ModifiableAttributeInstance knockbackRes = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackRes != null) {
            AttributeModifier existing = knockbackRes.getModifier(KNOCKBACK_MODIFIER_ID);
            if (existing != null) knockbackRes.removeModifier(existing);
            if (hasUnbreakable) knockbackRes.addPermanentModifier(new AttributeModifier(KNOCKBACK_MODIFIER_ID, "affix_kb", 1.0, AttributeModifier.Operation.ADDITION));
        }

        ModifiableAttributeInstance moveSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpeed != null) {
            AttributeModifier existing = moveSpeed.getModifier(SWIFTNESS_MODIFIER_ID);
            if (existing != null) moveSpeed.removeModifier(existing);
            if (hasSwiftness) moveSpeed.addPermanentModifier(new AttributeModifier(SWIFTNESS_MODIFIER_ID, "affix_swift", 0.05, AttributeModifier.Operation.MULTIPLY_BASE));

            AttributeModifier agilityMod = moveSpeed.getModifier(AGILITY_MODIFIER_ID);
            if (agilityMod != null) moveSpeed.removeModifier(agilityMod);
            if (hasAgility) moveSpeed.addPermanentModifier(new AttributeModifier(AGILITY_MODIFIER_ID, "affix_agility", 0.05, AttributeModifier.Operation.MULTIPLY_BASE));
        }

        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
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
                levels, xps,
                data.getMageRank(), data.getMageXp(), data.getKnownSpells()
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