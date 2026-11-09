package com.example.examplemod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

public class RpgFireballEntity extends SmallFireballEntity implements IRendersAsItem {

    private float customDamage = 10.0f;

    public RpgFireballEntity(EntityType<? extends SmallFireballEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public RpgFireballEntity(World worldIn, PlayerEntity shooter, double accelX, double accelY, double accelZ, float damage) {
        super(ModEntities.RPG_FIREBALL.get(), worldIn);
        this.customDamage = damage;
        this.setOwner(shooter);

        this.moveTo(shooter.getX(), shooter.getEyeY(), shooter.getZ(), shooter.yRot, shooter.xRot);
        // Запускаем шар со скоростью 2.0
        this.shoot(accelX, accelY, accelZ, 2.0F, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();

        // НОВОЕ: Проверяем время полета. 25 тиков * 2 блока/тик = 50 блоков (с запасом от 40)
        if (!this.level.isClientSide) {
            if (this.tickCount > 25) {
                // Создаем эффект взрыва перед исчезновением
                if (this.level instanceof ServerWorld) {
                    ((ServerWorld) this.level).sendParticles(ParticleTypes.EXPLOSION,
                            this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
                }
                this.remove();
            }
        }
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.FIRE_CHARGE);
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
            DamageSource damageSource = DamageSource.fireball(this, shooter);

            entity.hurt(damageSource, this.customDamage);
            entity.setSecondsOnFire(5);
        }
    }
}