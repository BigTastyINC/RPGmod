package com.example.examplemod.event;

import com.example.examplemod.entity.MobRarity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "examplemod")
public class MobLevelHandler {

    @SubscribeEvent
    public static void onMobSpawn(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof MobEntity)) return;
        if (!(event.getWorld() instanceof ServerWorld)) return;

        MobEntity mob = (MobEntity) event.getEntity();
        if (mob.hasCustomName()) return;

        // Жители (NPC) без редкости
        if (mob instanceof VillagerEntity) {
            double dxV = mob.getX() - 0.0;
            double dyV = mob.getY() - 64.0;
            double dzV = mob.getZ() - 0.0;
            double distanceV = Math.sqrt(dxV * dxV + dyV * dyV + dzV * dzV);
            int levelV = 1 + (int) (distanceV / 45.0);

            String finalNameV = "§7[Lvl " + levelV + "] " + mob.getName().getString();
            mob.setCustomName(new StringTextComponent(finalNameV));
            mob.setCustomNameVisible(true);
            mob.getPersistentData().putInt("RpgMobLevel", levelV);
            return;
        }

        double dx = mob.getX() - 0.0;
        double dy = mob.getY() - 64.0;
        double dz = mob.getZ() - 0.0;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        int baseLevel = 1 + (int) (distance / 45.0);
        Random rand = new Random();
        int finalLevel = Math.max(1, baseLevel + rand.nextInt(3) - 1);

        MobRarity rarity;

        if (mob instanceof AnimalEntity) {
            int roll = rand.nextInt(100);
            if (roll < 60) rarity = MobRarity.COMMON;
            else if (roll < 85) rarity = MobRarity.UNCOMMON;
            else if (roll < 95) rarity = MobRarity.RARE;
            else if (roll < 99) rarity = MobRarity.EPIC;
            else rarity = MobRarity.LEGENDARY;
        } else {
            int roll = rand.nextInt(100);
            if (roll < 85) rarity = MobRarity.COMMON;
            else if (roll < 95) rarity = MobRarity.LEADER;
            else rarity = MobRarity.LORD;
        }

        applyLevelToMob(mob, finalLevel, rarity);
    }

    private static void applyLevelToMob(MobEntity mob, int level, MobRarity rarity) {
        ModifiableAttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            double baseHealth = maxHealth.getBaseValue();
            double newHealth = baseHealth * (1.0 + (level - 1) * 0.5) * rarity.getHealthMultiplier();
            maxHealth.setBaseValue(newHealth);
            mob.setHealth((float) newHealth);
        }

        ModifiableAttributeInstance attackDamage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            double baseDamage = attackDamage.getBaseValue();
            double newDamage = baseDamage * (1.0 + (level - 1) * 0.2) * rarity.getDamageMultiplier();
            attackDamage.setBaseValue(newDamage);
        }

        String levelPrefix = "§7[Lvl " + level + "] ";
        String finalName;

        // Имя моба
        String mobName = mob.getName().getString();

        if (mob instanceof AnimalEntity) {
            // Животные: [Lvl 20] Лошадь ★★
            finalName = levelPrefix + mobName + rarity.getStars();
        } else {
            // Монстры: [Lvl 20] Зомби [Лорд]
            String prefix = rarity.getPrefix();
            if (!prefix.isEmpty()) {
                finalName = levelPrefix + mobName + " " + prefix;
            } else {
                finalName = levelPrefix + mobName;
            }
        }

        mob.setCustomName(new StringTextComponent(finalName));
        mob.setCustomNameVisible(true);

        mob.getPersistentData().putInt("RpgMobLevel", level);
        mob.getPersistentData().putInt("RpgMobRarity", rarity.ordinal());
    }
}