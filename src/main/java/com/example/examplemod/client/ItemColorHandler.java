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

        // === 1. ПОКРАСКА БРОНИ И ОРУЖИЯ (без изменений) ===
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

        // === 2. ПОКРАСКА ЗЕЛИЙ (без изменений) ===
        event.getItemColors().register((stack, tintIndex) -> {
            if (stack.getItem() instanceof RpgPotionItem) {
                if (((RpgPotionItem) stack.getItem()).isManaPotion()) return 0xFF0055FF;
                else return 0xFFFF0000;
            }
            return -1;
        }, ModItems.MANA_POTION.get(), ModItems.HEALTH_POTION.get());

        // === 3. ПОКРАСКА ТОМА ОБУЧЕНИЯ (НОВОЕ) ===
        event.getItemColors().register((stack, tintIndex) -> {
            CompoundNBT nbt = stack.getTag();
            if (nbt != null) {
                int level = nbt.getInt("SpellLevel");
                switch (level) {
                    case 1: return 0xFF55FF55; // 1 уровень - Зеленый (Необычный)
                    case 2: return 0xFF5555FF; // 2 уровень - Синий (Редкий)
                    case 3: return 0xFFAA00AA; // 3 уровень - Фиолетовый (Эпический)
                    case 4: return 0xFFFFAA00; // 4 уровень - Золотой (Легендарный)
                    case 5: return 0xFFFF3333; // 5 уровень - Красный (Архимаг)
                    default: return 0xFFFFFFFF; // 0 уровень - Без покраски (Обычный)
                }
            }
            return 0xFFFFFFFF; // Если NBT нет
        }, ModItems.SPELL_TOME.get());
    }
}