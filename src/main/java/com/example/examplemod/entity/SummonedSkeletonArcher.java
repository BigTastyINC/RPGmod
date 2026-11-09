package com.example.examplemod.entity;

import com.example.examplemod.entity.goal.FollowOwnerGoal;
import com.example.examplemod.entity.goal.OwnerHurtByTargetGoal;
import com.example.examplemod.entity.goal.OwnerHurtTargetGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

import java.util.Random;
import java.util.UUID;

public class SummonedSkeletonArcher extends SkeletonEntity {

    public SummonedSkeletonArcher(EntityType<? extends SkeletonEntity> type, World world) {
        super(type, world);
        this.setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SwimGoal(this));
        this.goalSelector.addGoal(2, new RangedBowAttackGoal<>(this, 1.0D, 20, 15.0F));
        // Следование за владельцем вместо случайной ходьбы
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.1D, 8.0F, 3.0F));
        this.goalSelector.addGoal(5, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(5, new LookRandomlyGoal(this));

        // Мстим за владельца, помогаем в атаке, мстим за себя, атакуем монстров
        this.targetSelector.addGoal(0, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, MonsterEntity.class, true));
    }

    @Override
    public boolean isSunBurnTick() {
        return false;
    }

    @Override
    public boolean canAttackType(EntityType<?> type) {
        if (type == ModEntities.SUMMONED_SKELETON_ARCHER.get()
                || type == ModEntities.SUMMONED_SKELETON_WARRIOR.get()
                || type == ModEntities.SUMMONED_ZOMBIE.get()) {
            return false;
        }
        return super.canAttackType(type);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        UUID owner = getOwnerUUID();
        if (owner != null && owner.equals(target.getUUID())) return false;
        return super.canAttack(target);
    }

    public void setOwnerUUID(UUID uuid) {
        this.getPersistentData().putUUID("OwnerUUID", uuid);
    }

    public UUID getOwnerUUID() {
        return this.getPersistentData().hasUUID("OwnerUUID") ? this.getPersistentData().getUUID("OwnerUUID") : null;
    }

    public void applyOwnerStats(PlayerEntity owner) {
        Random rand = new Random();
        double mult = 0.8 + rand.nextDouble() * 0.4;

        double halfHp = owner.getMaxHealth() / 2.0 * mult;
        double halfDmg = owner.getAttributeValue(Attributes.ATTACK_DAMAGE) / 2.0 * mult;

        ModifiableAttributeInstance hpAttr = this.getAttribute(Attributes.MAX_HEALTH);
        if (hpAttr != null) {
            hpAttr.setBaseValue(halfHp);
            this.setHealth((float) halfHp);
        }

        ModifiableAttributeInstance dmgAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmgAttr != null) {
            dmgAttr.setBaseValue(halfDmg);
        }

        this.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.BOW));
    }
}