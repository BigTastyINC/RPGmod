package com.example.examplemod.entity.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;
import java.util.UUID;

public class OwnerHurtByTargetGoal extends TargetGoal {
    private final MobEntity entity;
    private LivingEntity attacker;

    public OwnerHurtByTargetGoal(MobEntity entity) {
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

        this.attacker = owner.getLastHurtByMob();
        if (this.attacker == null) return false;
        if (this.attacker == owner) return false;
        if (this.attacker.isSpectator()) return false;

        return entity.canAttackType(this.attacker.getType());
    }

    @Override
    public void start() {
        entity.setTarget(this.attacker);
        super.start();
    }
}