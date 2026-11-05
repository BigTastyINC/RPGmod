package com.example.examplemod.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerLevelProvider implements ICapabilitySerializable<INBT> {
    private final IPlayerLevel instance = new PlayerLevel();
    private final LazyOptional<IPlayerLevel> optional = LazyOptional.of(() -> instance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        // Возвращаем наши данные, если запрашивают именно нашу Capability
        return ModCapabilities.PLAYER_LEVEL.orEmpty(cap, optional);
    }

    @Override
    public INBT serializeNBT() {
        return instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        if (nbt instanceof CompoundNBT) {
            instance.deserializeNBT((CompoundNBT) nbt);
        }
    }
}