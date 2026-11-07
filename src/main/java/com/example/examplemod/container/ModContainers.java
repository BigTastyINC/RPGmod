package com.example.examplemod.container;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainers {
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, "examplemod");

    public static final RegistryObject<ContainerType<TradeContainer>> TRADE_CONTAINER = CONTAINERS.register("trade_container",
            () -> IForgeContainerType.create(TradeContainer::new));
}