package com.example.examplemod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class RpgFrostboltEntity extends SnowballEntity implements IRendersAsItem {

    private float customDamage = 8.0f;

    public RpgFrostboltEntity(EntityType<? extends SnowballEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public RpgFrostboltEntity(World worldIn, PlayerEntity shooter, double accelX, double accelY, double accelZ, float damage) {
        super(ModEntities.RPG_FROSTBOLT.get(), worldIn);
        this.customDamage = damage;
        this.setOwner(shooter);
        this.moveTo(shooter.getX(), shooter.getEyeY(), shooter.getZ(), shooter.yRot, shooter.xRot);
        // Скорость 1.5, без разброса
        this.shoot(accelX, accelY, accelZ, 1.5F, 0.0F);
    }

    @Override
    public ItemStack getItem() {
        // Используем текстуру снежка как визуал для стрелы
        return new ItemStack(Items.SNOWBALL);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void onHitEntity(EntityRayTraceResult result) {
        if (!this.level.isClientSide) {
            Entity entity = result.getEntity();
            Entity shooter = this.getOwner();
            DamageSource damageSource = DamageSource.thrown(this, shooter);

            // Наносим урон
            entity.hurt(damageSource, this.customDamage);

            // Накладываем Замедление II на 5 секунд (100 тиков)
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 100, 1));
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Удаляем стрелу через 30 тиков (примерно 45 блоков пути)
        if (!this.level.isClientSide) {
            if (this.tickCount > 30) {
                this.remove();
            }
        }
    }
}