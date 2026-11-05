package com.example.examplemod.client;

import com.example.examplemod.item.ModItems;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "examplemod", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemColorHandler {

    @SubscribeEvent
    public static void registerItemColors(ColorHandlerEvent.Item event) {
        List<Item> colorableItems = new ArrayList<>();

        // Собираем все предметы, которые надо красить
        for (net.minecraftforge.fml.RegistryObject<Item> item : ModItems.ITEMS.getEntries()) {
            String name = item.getId().getPath();
            // Если это меч, топор или броня
            if (name.endsWith("_sword") || name.endsWith("_axe") || name.contains("_helmet") || name.contains("_chestplate") || name.contains("_leggings") || name.contains("_boots")) {
                colorableItems.add(item.get());
            }
        }

        event.getItemColors().register((stack, tintIndex) -> {
            String name = stack.getItem().getRegistryName().getPath();

            if (name.startsWith("silver")) return 0xFFFFFFFF;
            if (name.startsWith("cobalt")) return 0xFF008B8B;
            if (name.startsWith("mithril")) return 0xFF9900FF;
            if (name.startsWith("adamantium")) return 0xFF000000;
            if (name.startsWith("orichalcum")) return 0xFF556B2F;

            return -1;
        }, colorableItems.toArray(new Item[0]));
    }
}