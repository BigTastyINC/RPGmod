package com.example.examplemod.jewelry;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class JewelryInventoryProvider implements ICapabilitySerializable<INBT> {
    private final IJewelryInventory instance = new JewelryInventory();
    private final LazyOptional<IJewelryInventory> optional = LazyOptional.of(() -> instance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return ModJewelry.JEWELRY_CAP.orEmpty(cap, optional);
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