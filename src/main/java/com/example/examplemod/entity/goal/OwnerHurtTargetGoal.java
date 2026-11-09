package com.example.examplemod.entity.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;
import java.util.UUID;

public class OwnerHurtTargetGoal extends TargetGoal {
    private final MobEntity entity;
    private LivingEntity targetMob;

    public OwnerHurtTargetGoal(MobEntity entity) {
        super(entity, false);
        this.entity = entity;
        this.setFlags(EnumSet.of(net.minecraft.entity.ai.goal.Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (entity.level.isClientSide) return false;
        if (!entity.getPersistentData().hasUUID("OwnerUUID")) return false;
        UUID uuid = entity.getPersistentData().getUUID("OwnerUUID");
        PlayerEntity owner = entity.level.getPlayerByUUID(uuid);
        if (owner == null) return false;

        this.targetMob = owner.getLastHurtMob();
        if (this.targetMob == null) return false;
        if (this.targetMob == owner) return false;
        if (this.targetMob.isSpectator()) return false;

        return entity.canAttackType(this.targetMob.getType());
    }

    @Override
    public void start() {
        entity.setTarget(this.targetMob);
        super.start();
    }
}