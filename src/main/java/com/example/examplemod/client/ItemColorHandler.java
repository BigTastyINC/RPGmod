package com.example.examplemod.client;

import com.example.examplemod.item.ModItems;
import com.example.examplemod.item.RpgPotionItem;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
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
        for (net.minecraftforge.fml.RegistryObject<Item> item : ModItems.ITEMS.getEntries()) {
            String name = item.getId().getPath();
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

        event.getItemColors().register((stack, tintIndex) -> {
            if (stack.getItem() instanceof RpgPotionItem) {
                if (((RpgPotionItem) stack.getItem()).isManaPotion()) return 0xFF0055FF;
                else return 0xFFFF0000;
            }
            if (stack.getItem() == ModItems.LUCK_POTION.get()) return 0xFFFFD700;
            return -1;
        }, ModItems.MANA_POTION.get(), ModItems.HEALTH_POTION.get(), ModItems.LUCK_POTION.get());

        // Красим свиток на основе RarityLevel
        event.getItemColors().register((stack, tintIndex) -> {
            if (stack.getItem() == ModItems.SPELL_SCROLL.get()) {
                CompoundNBT nbt = stack.getTag();
                if (nbt != null && nbt.contains("RarityLevel")) {
                    int level = nbt.getInt("RarityLevel");
                    switch (level) {
                        case 1: return 0xFF55FF55; // Зеленый
                        case 2: return 0xFF5555FF; // Синий
                        case 3: return 0xFFAA00AA; // Фиолетовый
                        case 4: return 0xFFFFAA00; // Золотой
                        default: return 0xFFFFFFFF; // Белый
                    }
                }
            }
            return -1;
        }, ModItems.SPELL_SCROLL.get());

        // --- ПОКРАСКА ПОСОХОВ ---
        event.getItemColors().register((stack, tintIndex) -> {
            if (stack.getItem() == ModItems.SMALL_STAFF.get()) return 0xFF00FFFF; // Голубой (Малый)
            if (stack.getItem() == ModItems.MEDIUM_STAFF.get()) return 0xFFAA00AA; // Фиолетовый (Средний)
            if (stack.getItem() == ModItems.LARGE_STAFF.get()) return 0xFFFFAA00; // Золотой (Большой)
            return -1;
        }, ModItems.SMALL_STAFF.get(), ModItems.MEDIUM_STAFF.get(), ModItems.LARGE_STAFF.get());
    }
}