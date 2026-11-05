package com.example.examplemod.event;

import net.minecraft.entity.MobEntity;
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

        double dx = mob.getX() - 0.0;
        double dy = mob.getY() - 64.0;
        double dz = mob.getZ() - 0.0;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        int baseLevel = 1 + (int) (distance / 100.0);

        Random rand = new Random();
        int randomVariance = rand.nextInt(3) - 1;
        int finalLevel = Math.max(1, baseLevel + randomVariance);

        applyLevelToMob(mob, finalLevel);
    }

    private static void applyLevelToMob(MobEntity mob, int level) {
        // 1. Здоровье
        ModifiableAttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            double baseHealth = maxHealth.getBaseValue();
            double newHealth = baseHealth * (1.0 + (level - 1) * 0.5);
            maxHealth.setBaseValue(newHealth);
            mob.setHealth((float) newHealth);
        }

        // 2. Урон
        ModifiableAttributeInstance attackDamage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            double baseDamage = attackDamage.getBaseValue();
            double newDamage = baseDamage * (1.0 + (level - 1) * 0.2);
            attackDamage.setBaseValue(newDamage);
        }

        // 3. Имя над головой
        String levelPrefix = "[Lvl " + level + "] ";
        mob.setCustomName(new StringTextComponent(levelPrefix + mob.getName().getString()));
        mob.setCustomNameVisible(true);

        // --- НАШ КОД: Сохраняем уровень моба в его NBT данные ---
        mob.getPersistentData().putInt("RpgMobLevel", level);
    }
}