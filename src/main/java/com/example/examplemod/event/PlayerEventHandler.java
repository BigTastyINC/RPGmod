package com.example.examplemod.event;

import com.example.examplemod.capability.IPlayerLevel;
import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.capability.PlayerLevelProvider;
import com.example.examplemod.item.ItemStatHelper;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.SyncLevelPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
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

    private static String getMetalTypeFromItem(Item item) {
        if (item.getRegistryName() == null) return null;
        String name = item.getRegistryName().getPath();
        if (name.startsWith("silver")) return "silver";
        if (name.startsWith("cobalt")) return "cobalt";
        if (name.startsWith("mithril")) return "mithril";
        if (name.startsWith("adamantium")) return "adamantium";
        if (name.startsWith("orichalcum")) return "orichalcum";
        return null;
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
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getPlayer().level.isClientSide) return;
        PlayerEntity player = event.getPlayer();
        ItemStack craftedItem = event.getCrafting();

        String metal = getMetalTypeFromItem(craftedItem.getItem());
        if (metal != null) {
            craftedItem.getOrCreateTag().putString("MetalType", metal);
        }

        if (ItemStatHelper.isGear(craftedItem)) {
            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                ItemStatHelper.generateStats(craftedItem, data.getLevel());
                player.displayClientMessage(new StringTextComponent("§aВы создали предмет " + data.getLevel() + "-го уровня!"), false);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) return;

        if (event.player.tickCount % 20 == 0) {
            event.player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                boolean changed = false;
                if (data.getMana() < data.getMaxMana()) {
                    data.setMana(data.getMana() + 1 + data.getManaRegen());
                    changed = true;
                }
                applyStats(event.player, data);
                checkInventoryForGear(event.player, data);

                if (changed) {
                    syncData(event.player, data);
                }
            });
        }
    }

    private static void checkInventoryForGear(PlayerEntity player, IPlayerLevel data) {
        if (!(player instanceof ServerPlayerEntity)) return;

        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
            ItemStack stack = player.inventory.getItem(i);
            if (ItemStatHelper.isGear(stack) && (!stack.hasTag() || !stack.getTag().contains("ItemLevel"))) {
                String metal = getMetalTypeFromItem(stack.getItem());
                if (metal != null) {
                    stack.getOrCreateTag().putString("MetalType", metal);
                }
                ItemStatHelper.generateStats(stack, data.getLevel());
                ((ServerPlayerEntity) player).containerMenu.broadcastChanges();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(oldData -> {
                event.getPlayer().getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(newData -> {
                    newData.setLevel(oldData.getLevel());
                    newData.setXp(oldData.getXp());
                    newData.setStatPoints(oldData.getStatPoints());
                    newData.setStrength(oldData.getStrength());
                    newData.setDexterity(oldData.getDexterity());
                    newData.setIntelligence(oldData.getIntelligence());
                    newData.setLuck(oldData.getLuck());
                    newData.setVitality(oldData.getVitality());
                    newData.setMana(oldData.getMana());
                    newData.setHealthRegen(oldData.getHealthRegen());
                    newData.setManaRegen(oldData.getManaRegen());
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
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            boolean hasBook = false;
            for (int i = 0; i < serverPlayer.inventory.getContainerSize(); i++) {
                if (serverPlayer.inventory.getItem(i).getItem() == ModItems.STATS_BOOK.get()) {
                    hasBook = true;
                    break;
                }
            }
            if (!hasBook) {
                serverPlayer.inventory.add(new ItemStack(ModItems.STATS_BOOK.get()));
            }
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

        for (ItemStack armorStack : player.inventory.armor) {
            if (ItemStatHelper.isArmor(armorStack) && armorStack.hasTag()) {
                CompoundNBT nbt = armorStack.getTag();
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
                double bonusHealth = (totalVit - 1) * 2.0;
                maxHealth.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_ID, "rpg_health", bonusHealth, AttributeModifier.Operation.ADDITION));
            }
        }

        ModifiableAttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            AttributeModifier existing = attackDamage.getModifier(DAMAGE_MODIFIER_ID);
            if (existing != null) attackDamage.removeModifier(existing);
            if (totalStr > 1) {
                double bonusDamage = (totalStr - 1) * 0.5;
                attackDamage.addPermanentModifier(new AttributeModifier(DAMAGE_MODIFIER_ID, "rpg_strength", bonusDamage, AttributeModifier.Operation.ADDITION));
            }
        }

        ModifiableAttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            AttributeModifier existing = attackSpeed.getModifier(ATTACK_SPEED_MODIFIER_ID);
            if (existing != null) attackSpeed.removeModifier(existing);
            if (totalDex > 1) {
                double bonusSpeed = (totalDex - 1) * 0.1;
                attackSpeed.addPermanentModifier(new AttributeModifier(ATTACK_SPEED_MODIFIER_ID, "rpg_dexterity", bonusSpeed, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    private static void syncData(PlayerEntity player, IPlayerLevel data) {
        if (player instanceof ServerPlayerEntity) {
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                    new SyncLevelPacket(data.getLevel(), data.getXp(), data.getStatPoints(),
                            data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(), data.getVitality(), data.getMana(),
                            data.getHealthRegen(), data.getManaRegen())
            );
        }
    }
}