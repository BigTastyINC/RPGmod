package com.example.examplemod.event;

import com.example.examplemod.entity.MobRarity;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.item.Rarity;
import com.example.examplemod.profession.RarityRoller;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
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

        if (RANDOM.nextFloat() < 0.40f) {
            int rarityIndex = mob.getPersistentData().getInt("RpgMobRarity");
            MobRarity[] rarities = MobRarity.values();
            MobRarity mobRarity = rarities[Math.min(rarityIndex, rarities.length - 1)];

            // Получаем уровень моба
            int mobLevel = mob.getPersistentData().getInt("RpgMobLevel");
            if (mobLevel <= 0) mobLevel = 1;

            // Роллим редкость кристалла по уровню моба
            Rarity crystalRarity = RarityRoller.roll(mobLevel);

            ItemStack crystal = ItemStack.EMPTY;

            switch (mobRarity) {
                case COMMON:
                    crystal = new ItemStack(ModItems.SMALL_MAGIC_CRYSTAL.get());
                    break;
                case LEADER:
                    crystal = new ItemStack(ModItems.MEDIUM_MAGIC_CRYSTAL.get());
                    break;
                case LORD:
                    crystal = new ItemStack(ModItems.LARGE_MAGIC_CRYSTAL.get());
                    break;
            }

            if (!crystal.isEmpty()) {
                // Записываем уровень и редкость в NBT
                CompoundNBT nbt = crystal.getOrCreateTag();
                nbt.putInt("ItemLevel", mobLevel);
                nbt.putInt("RarityLevel", crystalRarity.ordinal());

                ItemEntity drop = new ItemEntity(mob.level, mob.getX(), mob.getY(), mob.getZ(), crystal);
                event.getDrops().add(drop);
            }
        }
    }
}