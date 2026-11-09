package com.example.examplemod;

import com.example.examplemod.block.ModBlocks;
import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.command.RpgCommand;
import com.example.examplemod.container.ModContainers;
import com.example.examplemod.entity.ModEntities;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.network.NetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mod("examplemod")
public class ExampleMod {

    public ExampleMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // Регистрация DeferredRegister
        ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus()); // <--- ВАЖНО!
        ModContainers.CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModEntities.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());

        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            RpgCommand.register(event.getDispatcher());
        });
    }

    private void removeHealthLimit() {
        try {
            Field field = RangedAttribute.class.getDeclaredField("maxValue");
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.setDouble(Attributes.MAX_HEALTH, 5000000.0);
        } catch (Exception e) {
            try {
                Field field = RangedAttribute.class.getDeclaredField("field_233752_b_");
                field.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                field.setDouble(Attributes.MAX_HEALTH, 5000000.0);
            } catch (Exception ex) {}
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        removeHealthLimit();
        ModCapabilities.register();
        NetworkHandler.register();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {}
    }
}