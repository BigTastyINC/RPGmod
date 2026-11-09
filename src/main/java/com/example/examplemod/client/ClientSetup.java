package com.example.examplemod.client;

import com.example.examplemod.container.ModContainers;
import com.example.examplemod.entity.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "examplemod", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(KeyBindings.OPEN_STATS);
        ScreenManager.register(ModContainers.TRADE_CONTAINER.get(), TradeScreen::new);

        RenderingRegistry.registerEntityRenderingHandler(ModEntities.RPG_FIREBALL.get(),
                manager -> new SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));

        RenderingRegistry.registerEntityRenderingHandler(ModEntities.RPG_FROSTBOLT.get(),
                manager -> new SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));

        // НОВОЕ: Рендереры призывов
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.SUMMONED_SKELETON_ARCHER.get(), SkeletonRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.SUMMONED_SKELETON_WARRIOR.get(), SkeletonRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.SUMMONED_ZOMBIE.get(), ZombieRenderer::new);
    }
}