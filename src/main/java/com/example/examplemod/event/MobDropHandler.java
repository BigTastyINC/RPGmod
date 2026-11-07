package com.example.examplemod.event;

import com.example.examplemod.entity.MobRarity;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.item.Rarity;
import com.example.examplemod.profession.RarityRoller;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
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

        // ===== 1. ЛОГИКА ДЛЯ ЖИВОТНЫХ (Мясо) =====
        if (mob instanceof AnimalEntity) {
            for (ItemEntity dropEntity : event.getDrops()) {
                ItemStack stack = dropEntity.getItem();

                // Если выпадает еда (сырое мясо)
                if (stack.getItem().isEdible()) {
                    CompoundNBT nbt = stack.getOrCreateTag();
                    nbt.putInt("ItemLevel", mobLevel);

                    int meatRarityIndex = 0; // По умолчанию обычное (0)
                    int roll = RANDOM.nextInt(100);

                    // ТВОЯ МАТЕМАТИКА ШАНСОВ
                    if (mobRarity == MobRarity.UNCOMMON) {
                        meatRarityIndex = (roll < 90) ? 1 : 0; // 90% необычное, 10% обычное
                    } else if (mobRarity == MobRarity.RARE) {
                        meatRarityIndex = (roll < 90) ? 2 : 1; // 90% редкое, 10% необычное
                    } else if (mobRarity == MobRarity.EPIC) {
                        meatRarityIndex = (roll < 90) ? 3 : 2; // 90% эпическое, 10% редкое
                    } else if (mobRarity == MobRarity.LEGENDARY) {
                        meatRarityIndex = (roll < 40) ? 4 : 3; // 40% легендарное, 60% эпическое
                    }

                    nbt.putInt("RarityLevel", meatRarityIndex);
                }
            }
        }

        // ===== 2. СТАРАЯ ЛОГИКА ДЛЯ КРИСТАЛЛОВ =====
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