package com.example.examplemod.event;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.entity.MobRarity;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.item.Rarity;
import com.example.examplemod.profession.RarityRoller;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "examplemod")
public class MobDropHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onMobDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof PlayerEntity)) return;
        if (!(event.getEntityLiving() instanceof MobEntity)) return;

        PlayerEntity player = (PlayerEntity) event.getSource().getEntity();
        MobEntity mob = (MobEntity) event.getEntityLiving();

        final int[] luck = {0};
        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> luck[0] = data.getLuck());

        // ИЗМЕНЕНО: Теперь 1 удача дает +0.01% (было 0.1%), влияние уменьшено в 10 раз
        float luckBonus = luck[0] * 0.0001f;

        int rarityIndex = mob.getPersistentData().getInt("RpgMobRarity");
        MobRarity[] rarities = MobRarity.values();
        MobRarity mobRarity = rarities[Math.min(rarityIndex, rarities.length - 1)];

        int mobLevel = mob.getPersistentData().getInt("RpgMobLevel");
        if (mobLevel <= 0) mobLevel = 1;

        boolean isLeader = mobRarity == MobRarity.LEADER;
        boolean isLord = mobRarity == MobRarity.LORD;

        // ===== 1. Том Телепорта =====
        if (isLord && RANDOM.nextFloat() < 0.02f + (luckBonus * 0.5f)) {
            ItemStack tome = new ItemStack(ModItems.SPELL_TOME.get());
            CompoundNBT nbt = tome.getOrCreateTag();
            nbt.putString("SpellId", "teleport_ore");
            nbt.putInt("SpellLevel", 4);
            event.getDrops().add(new ItemEntity(mob.level, mob.getX(), mob.getY(), mob.getZ(), tome));
        }

        // ===== 2. Том Крафта реликвии =====
        if (isLord && mobLevel >= 150 && RANDOM.nextFloat() < 0.01f + (luckBonus * 0.1f)) {
            ItemStack tome = new ItemStack(ModItems.SPELL_TOME.get());
            CompoundNBT nbt = tome.getOrCreateTag();
            nbt.putString("SpellId", "craft_relic");
            nbt.putInt("SpellLevel", 5);
            event.getDrops().add(new ItemEntity(mob.level, mob.getX(), mob.getY(), mob.getZ(), tome));
        }

        // ===== 3. Том Призыва торговца =====
        if (isLeader) {
            if (RANDOM.nextFloat() < 0.10f + luckBonus) {
                ItemStack tome = new ItemStack(ModItems.SPELL_TOME.get());
                CompoundNBT nbt = tome.getOrCreateTag();
                nbt.putString("SpellId", "summon_merchant");
                nbt.putInt("SpellLevel", 1);
                event.getDrops().add(new ItemEntity(mob.level, mob.getX(), mob.getY(), mob.getZ(), tome));
            }
        } else if (!isLord) {
            if (RANDOM.nextFloat() < 0.05f + luckBonus) {
                ItemStack tome = new ItemStack(ModItems.SPELL_TOME.get());
                CompoundNBT nbt = tome.getOrCreateTag();
                nbt.putString("SpellId", "summon_merchant");
                nbt.putInt("SpellLevel", 1);
                event.getDrops().add(new ItemEntity(mob.level, mob.getX(), mob.getY(), mob.getZ(), tome));
            }
        }

        // ===== 4. Том Призыва нежити =====
        if (mob instanceof SkeletonEntity && isLord && mobLevel >= 50 && RANDOM.nextFloat() < 0.10f + luckBonus) {
            ItemStack tome = new ItemStack(ModItems.SPELL_TOME.get());
            CompoundNBT nbt = tome.getOrCreateTag();
            nbt.putString("SpellId", "summon_undead");
            nbt.putInt("SpellLevel", 1);
            event.getDrops().add(new ItemEntity(mob.level, mob.getX(), mob.getY(), mob.getZ(), tome));
        }

        // ===== 5. Дроп снаряжения =====
        if (!isLord && !isLeader) {
            if (RANDOM.nextFloat() < 0.17f + luckBonus) {
                ItemStack armor = randomArmor(0);
                if (armor != null) {
                    Rarity r = RarityRoller.roll(mobLevel, luck[0]);
                    com.example.examplemod.item.ItemStatHelper.generateStatsWithRarity(armor, mobLevel, r);
                    event.getDrops().add(new ItemEntity(mob.level, mob.getX(), mob.getY(), mob.getZ(), armor));
                }
            }
        } else if (isLeader) {
            if (RANDOM.nextFloat() < 0.14f + luckBonus) {
                ItemStack gear = RANDOM.nextBoolean() ? randomArmor(1) : randomWeapon(1);
                if (gear != null) {
                    Rarity r = RarityRoller.roll(mobLevel, luck[0]);
                    com.example.examplemod.item.ItemStatHelper.generateStatsWithRarity(gear, mobLevel, r);
                    event.getDrops().add(new ItemEntity(mob.level, mob.getX(), mob.getY(), mob.getZ(), gear));
                }
            }
        } else {
            if (RANDOM.nextFloat() < 0.10f + luckBonus) {
                ItemStack gear = RANDOM.nextBoolean() ? randomArmor(2) : randomWeapon(2);
                if (gear != null) {
                    Rarity r = RarityRoller.roll(mobLevel, luck[0]);
                    com.example.examplemod.item.ItemStatHelper.generateStatsWithRarity(gear, mobLevel, r);
                    event.getDrops().add(new ItemEntity(mob.level, mob.getX(), mob.getY(), mob.getZ(), gear));
                }
            }
        }

        // ===== 6. Животные (Мясо) =====
        if (mob instanceof net.minecraft.entity.passive.AnimalEntity) {
            for (ItemEntity dropEntity : event.getDrops()) {
                ItemStack stack = dropEntity.getItem();
                if (stack.getItem().isEdible()) {
                    CompoundNBT nbt = stack.getOrCreateTag();
                    nbt.putInt("ItemLevel", mobLevel);
                    int meatRarityIndex = 0;
                    int roll = RANDOM.nextInt(100);
                    if (mobRarity == MobRarity.UNCOMMON) meatRarityIndex = (roll < 90) ? 1 : 0;
                    else if (mobRarity == MobRarity.RARE) meatRarityIndex = (roll < 90) ? 2 : 1;
                    else if (mobRarity == MobRarity.EPIC) meatRarityIndex = (roll < 90) ? 3 : 2;
                    else if (mobRarity == MobRarity.LEGENDARY) meatRarityIndex = (roll < 40) ? 4 : 3;
                    nbt.putInt("RarityLevel", meatRarityIndex);
                }
            }
        }

        // ===== 7. Кристаллы =====
        if (RANDOM.nextFloat() < 0.40f + luckBonus) {
            Rarity crystalRarity = RarityRoller.roll(mobLevel, luck[0]);
            ItemStack crystal = ItemStack.EMPTY;
            switch (mobRarity) {
                case COMMON: case UNCOMMON: case LEADER:
                    crystal = new ItemStack(ModItems.SMALL_MAGIC_CRYSTAL.get()); break;
                case RARE:
                    crystal = new ItemStack(ModItems.MEDIUM_MAGIC_CRYSTAL.get()); break;
                case EPIC: case LEGENDARY: case LORD:
                    crystal = new ItemStack(ModItems.LARGE_MAGIC_CRYSTAL.get()); break;
            }
            if (!crystal.isEmpty()) {
                CompoundNBT nbt = crystal.getOrCreateTag();
                nbt.putInt("ItemLevel", mobLevel);
                nbt.putInt("RarityLevel", crystalRarity.ordinal());
                event.getDrops().add(new ItemEntity(mob.level, mob.getX(), mob.getY(), mob.getZ(), crystal));
            }
        }
    }

    private static ItemStack randomArmor(int tier) {
        net.minecraft.item.Item[] pool;
        if (tier == 0) {
            pool = new net.minecraft.item.Item[]{
                    net.minecraft.item.Items.LEATHER_HELMET, net.minecraft.item.Items.LEATHER_CHESTPLATE, net.minecraft.item.Items.LEATHER_LEGGINGS, net.minecraft.item.Items.LEATHER_BOOTS,
                    net.minecraft.item.Items.GOLDEN_HELMET, net.minecraft.item.Items.GOLDEN_CHESTPLATE, net.minecraft.item.Items.GOLDEN_LEGGINGS, net.minecraft.item.Items.GOLDEN_BOOTS,
                    net.minecraft.item.Items.IRON_HELMET, net.minecraft.item.Items.IRON_CHESTPLATE, net.minecraft.item.Items.IRON_LEGGINGS, net.minecraft.item.Items.IRON_BOOTS
            };
        } else if (tier == 1) {
            pool = new net.minecraft.item.Item[]{
                    net.minecraft.item.Items.IRON_HELMET, net.minecraft.item.Items.IRON_CHESTPLATE, net.minecraft.item.Items.IRON_LEGGINGS, net.minecraft.item.Items.IRON_BOOTS,
                    net.minecraft.item.Items.GOLDEN_HELMET, net.minecraft.item.Items.GOLDEN_CHESTPLATE, net.minecraft.item.Items.GOLDEN_LEGGINGS, net.minecraft.item.Items.GOLDEN_BOOTS,
                    net.minecraft.item.Items.DIAMOND_HELMET, net.minecraft.item.Items.DIAMOND_CHESTPLATE, net.minecraft.item.Items.DIAMOND_LEGGINGS, net.minecraft.item.Items.DIAMOND_BOOTS,
                    ModItems.COBALT_HELMET.get(), ModItems.COBALT_CHESTPLATE.get(), ModItems.COBALT_LEGGINGS.get(), ModItems.COBALT_BOOTS.get()
            };
        } else {
            pool = new net.minecraft.item.Item[]{
                    net.minecraft.item.Items.DIAMOND_HELMET, net.minecraft.item.Items.DIAMOND_CHESTPLATE, net.minecraft.item.Items.DIAMOND_LEGGINGS, net.minecraft.item.Items.DIAMOND_BOOTS,
                    ModItems.COBALT_HELMET.get(), ModItems.COBALT_CHESTPLATE.get(), ModItems.COBALT_LEGGINGS.get(), ModItems.COBALT_BOOTS.get(),
                    ModItems.MITHRIL_HELMET.get(), ModItems.MITHRIL_CHESTPLATE.get(), ModItems.MITHRIL_LEGGINGS.get(), ModItems.MITHRIL_BOOTS.get(),
                    ModItems.ADAMANTIUM_HELMET.get(), ModItems.ADAMANTIUM_CHESTPLATE.get(), ModItems.ADAMANTIUM_LEGGINGS.get(), ModItems.ADAMANTIUM_BOOTS.get(),
                    ModItems.ORICHALCUM_HELMET.get(), ModItems.ORICHALCUM_CHESTPLATE.get(), ModItems.ORICHALCUM_LEGGINGS.get(), ModItems.ORICHALCUM_BOOTS.get()
            };
        }
        if (pool.length == 0) return null;
        return new ItemStack(pool[RANDOM.nextInt(pool.length)]);
    }

    private static ItemStack randomWeapon(int tier) {
        net.minecraft.item.Item[] pool;
        if (tier == 0) {
            pool = new net.minecraft.item.Item[]{net.minecraft.item.Items.IRON_SWORD, net.minecraft.item.Items.IRON_AXE};
        } else if (tier == 1) {
            pool = new net.minecraft.item.Item[]{
                    net.minecraft.item.Items.IRON_SWORD, net.minecraft.item.Items.IRON_AXE,
                    net.minecraft.item.Items.DIAMOND_SWORD, net.minecraft.item.Items.DIAMOND_AXE,
                    ModItems.COBALT_SWORD.get(), ModItems.COBALT_AXE.get()
            };
        } else {
            pool = new net.minecraft.item.Item[]{
                    net.minecraft.item.Items.DIAMOND_SWORD, net.minecraft.item.Items.DIAMOND_AXE,
                    ModItems.COBALT_SWORD.get(), ModItems.COBALT_AXE.get(),
                    ModItems.MITHRIL_SWORD.get(), ModItems.MITHRIL_AXE.get(),
                    ModItems.ADAMANTIUM_SWORD.get(), ModItems.ADAMANTIUM_AXE.get(),
                    ModItems.ORICHALCUM_SWORD.get(), ModItems.ORICHALCUM_AXE.get()
            };
        }
        if (pool.length == 0) return null;
        return new ItemStack(pool[RANDOM.nextInt(pool.length)]);
    }
}