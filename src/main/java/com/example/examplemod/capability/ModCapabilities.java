package com.example.examplemod.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;

public class ModCapabilities {
    // В Forge 1.16.5 используем аннотацию @CapabilityInject
    @CapabilityInject(IPlayerLevel.class)
    public static Capability<IPlayerLevel> PLAYER_LEVEL = null;

    // Метод для регистрации (вызывается из ExampleMod)
    public static void register() {
        CapabilityManager.INSTANCE.register(
                IPlayerLevel.class,
                new Capability.IStorage<IPlayerLevel>() {
                    @Override
                    public CompoundNBT writeNBT(Capability<IPlayerLevel> capability, IPlayerLevel instance, Direction side) {
                        return instance.serializeNBT();
                    }

                    @Override
                    public void readNBT(Capability<IPlayerLevel> capability, IPlayerLevel instance, Direction side, net.minecraft.nbt.INBT nbt) {
                        instance.deserializeNBT((CompoundNBT) nbt);
                    }
                },
                PlayerLevel::new
        );
    }

    // Удобный метод для получения уровня игрока
    public static LazyOptional<IPlayerLevel> getPlayerLevel(net.minecraft.entity.player.PlayerEntity player) {
        return player.getCapability(PLAYER_LEVEL);
    }
}