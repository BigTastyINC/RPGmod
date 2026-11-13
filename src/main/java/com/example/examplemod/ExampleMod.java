package com.example.examplemod;

import com.example.examplemod.block.ModBlocks;
import com.example.examplemod.block.ModTileEntities;
import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.command.RpgCommand;
import com.example.examplemod.container.ModContainers;
import com.example.examplemod.entity.ModEntities;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.jewelry.ModJewelry;
import com.example.examplemod.jewelry.ModJewelryItems;
import com.example.examplemod.network.NetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mod("examplemod")
public class ExampleMod {

    public ExampleMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);

        // Регистрация DeferredRegister
        ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModJewelryItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModTileEntities.TILE_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus()); // <-- ДОБАВЛЕНО
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
        ModJewelry.register();
        NetworkHandler.register();
    }

    // Регистрация слотов в Curios
    private void enqueueIMC(final InterModEnqueueEvent event) {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE,
                () -> SlotTypePreset.RING.getMessageBuilder().size(2).build());
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE,
                () -> SlotTypePreset.NECKLACE.getMessageBuilder().size(1).build());
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE,
                () -> SlotTypePreset.CHARM.getMessageBuilder().size(2).build());
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE,
                () -> SlotTypePreset.BRACELET.getMessageBuilder().size(2).build());
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {}
    }
}