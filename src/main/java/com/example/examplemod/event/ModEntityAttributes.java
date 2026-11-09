package com.example.examplemod.event;

import com.example.examplemod.entity.ModEntities;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "examplemod", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityAttributes {

    @SubscribeEvent
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SUMMONED_SKELETON_ARCHER.get(), createSkeletonAttributes().build());
        event.put(ModEntities.SUMMONED_SKELETON_WARRIOR.get(), createSkeletonAttributes().build());
        event.put(ModEntities.SUMMONED_ZOMBIE.get(), createZombieAttributes().build());
    }

    private static AttributeModifierMap.MutableAttribute createSkeletonAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    private static AttributeModifierMap.MutableAttribute createZombieAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                // НОВОЕ: Без этого атрибута ZombieEntity.hurt() даёт NPE
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0D);
    }
}