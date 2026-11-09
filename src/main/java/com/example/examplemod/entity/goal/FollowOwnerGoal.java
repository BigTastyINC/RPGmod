package com.example.examplemod.entity.goal;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;
import java.util.UUID;

public class FollowOwnerGoal extends Goal {
    private final MobEntity entity;
    private final double speed;
    private final float minDist;
    private final float maxDist;
    private PlayerEntity owner;

    public FollowOwnerGoal(MobEntity entity, double speed, float minDist, float maxDist) {
        this.entity = entity;
        this.speed = speed;
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    private PlayerEntity getOwner() {
        if (entity.level.isClientSide) return null;
        if (!entity.getPersistentData().hasUUID("OwnerUUID")) return null;
        UUID uuid = entity.getPersistentData().getUUID("OwnerUUID");
        return entity.level.getPlayerByUUID(uuid);
    }

    @Override
    public boolean canUse() {
        PlayerEntity o = getOwner();
        if (o == null || o.isSpectator()) return false;
        // Если владелец слишком далеко — начинаем догонять
        if (entity.distanceToSqr(o) < (double) (maxDist * maxDist)) return false;
        this.owner = o;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (owner == null || owner.removed) return false;
        return entity.getNavigation().isDone() ? false : entity.distanceToSqr(owner) > (double) (minDist * minDist);
    }

    @Override
    public void start() {
        entity.getNavigation().moveTo(owner, speed);
    }

    @Override
    public void stop() {
        owner = null;
        entity.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (owner == null) return;
        // Если далеко (>16 блоков) — телепортируемся к владельцу
        if (entity.distanceToSqr(owner) > 256.0D) {
            entity.teleportTo(owner.getX(), owner.getY(), owner.getZ());
        } else {
            entity.getNavigation().moveTo(owner, speed);
        }
    }
}