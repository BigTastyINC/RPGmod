package com.example.examplemod.jewelry;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class ModJewelry {

    @CapabilityInject(IJewelryInventory.class)
    public static Capability<IJewelryInventory> JEWELRY_CAP = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(
                IJewelryInventory.class,
                new Capability.IStorage<IJewelryInventory>() {
                    @Override
                    public CompoundNBT writeNBT(Capability<IJewelryInventory> capability, IJewelryInventory instance, Direction side) {
                        return instance.serializeNBT();
                    }

                    @Override
                    public void readNBT(Capability<IJewelryInventory> capability, IJewelryInventory instance, Direction side, INBT nbt) {
                        if (nbt instanceof CompoundNBT) instance.deserializeNBT((CompoundNBT) nbt);
                    }
                },
                JewelryInventory::new
        );
    }
}