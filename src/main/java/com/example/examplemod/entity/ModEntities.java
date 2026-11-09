package com.example.examplemod.entity;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, "examplemod");

    public static final RegistryObject<EntityType<RpgFireballEntity>> RPG_FIREBALL =
            ENTITIES.register("rpg_fireball",
                    () -> EntityType.Builder.<RpgFireballEntity>of(RpgFireballEntity::new, EntityClassification.MISC)
                            .sized(0.3125F, 0.3125F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("rpg_fireball"));

    public static final RegistryObject<EntityType<RpgFrostboltEntity>> RPG_FROSTBOLT =
            ENTITIES.register("rpg_frostbolt",
                    () -> EntityType.Builder.<RpgFrostboltEntity>of(RpgFrostboltEntity::new, EntityClassification.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("rpg_frostbolt"));

    // НОВОЕ: Призыв нежити
    public static final RegistryObject<EntityType<SummonedSkeletonArcher>> SUMMONED_SKELETON_ARCHER =
            ENTITIES.register("summoned_skeleton_archer",
                    () -> EntityType.Builder.<SummonedSkeletonArcher>of(SummonedSkeletonArcher::new, EntityClassification.MONSTER)
                            .sized(0.6F, 1.99F)
                            .clientTrackingRange(8)
                            .build("summoned_skeleton_archer"));

    public static final RegistryObject<EntityType<SummonedSkeletonWarrior>> SUMMONED_SKELETON_WARRIOR =
            ENTITIES.register("summoned_skeleton_warrior",
                    () -> EntityType.Builder.<SummonedSkeletonWarrior>of(SummonedSkeletonWarrior::new, EntityClassification.MONSTER)
                            .sized(0.6F, 1.99F)
                            .clientTrackingRange(8)
                            .build("summoned_skeleton_warrior"));

    public static final RegistryObject<EntityType<SummonedZombie>> SUMMONED_ZOMBIE =
            ENTITIES.register("summoned_zombie",
                    () -> EntityType.Builder.<SummonedZombie>of(SummonedZombie::new, EntityClassification.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .build("summoned_zombie"));
}