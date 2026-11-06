package com.example.examplemod;

import com.example.examplemod.block.ModBlocks;
import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.client.KeyBindings;
import com.example.examplemod.command.RpgCommand;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.network.NetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mod("examplemod")
public class ExampleMod
{
    private static final Logger LOGGER = LogManager.getLogger();

    public ExampleMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());

        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            RpgCommand.register(event.getDispatcher());
        });

        removeHealthLimit();
    }

    private void removeHealthLimit() {
        try {
            Field field = RangedAttribute.class.getDeclaredField("maxValue");
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.setDouble(Attributes.MAX_HEALTH, 1000000.0);
        } catch (Exception e) {
            try {
                Field field = RangedAttribute.class.getDeclaredField("field_233752_b_");
                field.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                field.setDouble(Attributes.MAX_HEALTH, 1000000.0);
            } catch (Exception ex) {}
        }
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        ModCapabilities.register();
        NetworkHandler.register();
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(KeyBindings.OPEN_STATS);
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {}
    }
}