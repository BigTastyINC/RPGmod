package com.example.examplemod.event;

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

        MobEntity mob = (MobEntity) event.getEntityLiving();

        int rarityIndex = mob.getPersistentData().getInt("RpgMobRarity");
        MobRarity[] rarities = MobRarity.values();
        MobRarity mobRarity = rarities[Math.min(rarityIndex, rarities.length - 1)];

        int mobLevel = mob.getPersistentData().getInt("RpgMobLevel");
        if (mobLevel <= 0) mobLevel = 1;

        // ===== 1. ДРОП ТОМА ТЕЛЕПОРТА С ЛОРДОВ (Шанс 2%) =====
        if (mobRarity == MobRarity.LORD && RANDOM.nextFloat() < 0.02f) {
            ItemStack tome = new ItemStack(ModItems.SPELL_TOME.get());
            CompoundNBT nbt = tome.getOrCreateTag();
            nbt.putString("SpellId", "teleport_ore");
            nbt.putInt("SpellLevel", 4);

            ItemEntity drop = new ItemEntity(mob.level, mob.getX(), mob.getY(), mob.getZ(), tome);
            event.getDrops().add(drop);
        }

        // ===== 2. ДРОП ТОМА КРАФТА РЕЛИКВИИ (Шанс 1%, Лорд 150+ ур.) =====
        if (mobRarity == MobRarity.LORD && mobLevel >= 150 && RANDOM.nextFloat() < 0.01f) {
            ItemStack tome = new ItemStack(ModItems.SPELL_TOME.get());
            CompoundNBT nbt = tome.getOrCreateTag();
            nbt.putString("SpellId", "craft_relic");
            nbt.putInt("SpellLevel", 5);

            ItemEntity drop = new ItemEntity(mob.level, mob.getX(), mob.getY(), mob.getZ(), tome);
            event.getDrops().add(drop);
        }

        // ===== 3. ДРОП ТОМА ПРИЗЫВА ТОРГОВЦА =====
        boolean isLeader = mobRarity == MobRarity.LEADER;
        boolean isLord = mobRarity == MobRarity.LORD;

        if (isLeader) {
            // С Предводителей 10%
            if (RANDOM.nextFloat() < 0.10f) {
                ItemStack tome = new ItemStack(ModItems.SPELL_TOME.get());
                CompoundNBT nbt = tome.getOrCreateTag();
                nbt.putString("SpellId", "summon_merchant");
                nbt.putInt("SpellLevel", 1);

                ItemEntity drop = new ItemEntity(mob.level, mob.getX(), mob.getY(), mob.getZ(), tome);
                event.getDrops().add(drop);
            }
        } else if (!isLord) {
            // С обычных мобов (все, кроме Лордов и Предводителей) 5%
            if (RANDOM.nextFloat() < 0.05f) {
                ItemStack tome = new ItemStack(ModItems.SPELL_TOME.get());
                CompoundNBT nbt = tome.getOrCreateTag();
                nbt.putString("SpellId", "summon_merchant");
                nbt.putInt("SpellLevel", 1);

                ItemEntity drop = new ItemEntity(mob.level, mob.getX(), mob.getY(), mob.getZ(), tome);
                event.getDrops().add(drop);
            }
        }

        // ===== 4. ДРОП ТОМА ПРИЗЫВА НЕЖИТИ (Скелет-Лорд 50+ ур., шанс 10%) =====
        if (mob instanceof SkeletonEntity && mobRarity == MobRarity.LORD && mobLevel >= 50) {
            if (RANDOM.nextFloat() < 0.10f) {
                ItemStack tome = new ItemStack(ModItems.SPELL_TOME.get());
                CompoundNBT nbt = tome.getOrCreateTag();
                nbt.putString("SpellId", "summon_undead");
                nbt.putInt("SpellLevel", 1);

                ItemEntity drop = new ItemEntity(mob.level, mob.getX(), mob.getY(), mob.getZ(), tome);
                event.getDrops().add(drop);
            }
        }

        // ===== 5. ЛОГИКА ДЛЯ ЖИВОТНЫХ (Мясо) =====
        if (mob instanceof net.minecraft.entity.passive.AnimalEntity) {
            for (ItemEntity dropEntity : event.getDrops()) {
                ItemStack stack = dropEntity.getItem();

                if (stack.getItem().isEdible()) {
                    CompoundNBT nbt = stack.getOrCreateTag();
                    nbt.putInt("ItemLevel", mobLevel);

                    int meatRarityIndex = 0;
                    int roll = RANDOM.nextInt(100);

                    if (mobRarity == MobRarity.UNCOMMON) {
                        meatRarityIndex = (roll < 90) ? 1 : 0;
                    } else if (mobRarity == MobRarity.RARE) {
                        meatRarityIndex = (roll < 90) ? 2 : 1;
                    } else if (mobRarity == MobRarity.EPIC) {
                        meatRarityIndex = (roll < 90) ? 3 : 2;
                    } else if (mobRarity == MobRarity.LEGENDARY) {
                        meatRarityIndex = (roll < 40) ? 4 : 3;
                    }

                    nbt.putInt("RarityLevel", meatRarityIndex);
                }
            }
        }

        // ===== 6. ДРОП КРИСТАЛЛОВ =====
        if (RANDOM.nextFloat() < 0.40f) {
            Rarity crystalRarity = RarityRoller.roll(mobLevel);

            ItemStack crystal = ItemStack.EMPTY;

            switch (mobRarity) {
                case COMMON:
                case UNCOMMON:
                case LEADER:
                    crystal = new ItemStack(ModItems.SMALL_MAGIC_CRYSTAL.get());
                    break;
                case RARE:
                    crystal = new ItemStack(ModItems.MEDIUM_MAGIC_CRYSTAL.get());
                    break;
                case EPIC:
                case LEGENDARY:
                case LORD:
                    crystal = new ItemStack(ModItems.LARGE_MAGIC_CRYSTAL.get());
                    break;
            }

            if (!crystal.isEmpty()) {
                CompoundNBT nbt = crystal.getOrCreateTag();
                nbt.putInt("ItemLevel", mobLevel);
                nbt.putInt("RarityLevel", crystalRarity.ordinal());

                ItemEntity drop = new ItemEntity(mob.level, mob.getX(), mob.getY(), mob.getZ(), crystal);
                event.getDrops().add(drop);
            }
        }
    }
}