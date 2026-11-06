package com.example.examplemod.event;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.profession.Profession;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "examplemod")
public class ProfessionEventHandler {

    // === РЫБАК (Ловля рыбы) ===
    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player == null || player.level.isClientSide) return;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            // Даем 20 опыта за каждый улов
            data.addProfessionXp(Profession.FISHERMAN, 20);
            int lvl = data.getProfessionLevel(Profession.FISHERMAN);

            // Шанс на x5 улов (0.5% за уровень, на 100 = 50%)
            float chance = lvl * 0.005f;
            if (player.level.random.nextFloat() < chance) {
                List<ItemStack> drops = event.getDrops();
                List<ItemStack> extraDrops = new ArrayList<>();

                for (ItemStack stack : drops) {
                    if (stack.getItem().isEdible() || stack.getItem() == Items.COD || stack.getItem() == Items.SALMON) {
                        ItemStack copy = stack.copy();
                        copy.setCount(copy.getCount() * 4); // 1 оригинал + 4 = x5
                        extraDrops.add(copy);
                    }
                }
                drops.addAll(extraDrops);
                player.displayClientMessage(new StringTextComponent("§bРыбак: Улов x5!"), true);
            }
            PlayerEventHandler.syncData(player, data);
        });
    }

    // === РУДОКОП (Копание руды) ===
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player == null || player.level.isClientSide) return;

        Block block = event.getState().getBlock();
        if (block.getRegistryName() == null) return;

        // Проверяем, что это руда
        String blockName = block.getRegistryName().getPath();
        if (!blockName.contains("ore")) return;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            // Даем 15 опыта за каждую руду
            data.addProfessionXp(Profession.MINER, 15);
            int lvl = data.getProfessionLevel(Profession.MINER);

            // Шанс на x4 дроп (0.5% за уровень, на 100 = 50%)
            float chance = lvl * 0.005f;
            if (player.level.random.nextFloat() < chance) {
                // Получаем стандартный дроп этого блока
                List<ItemStack> drops = Block.getDrops(event.getState(), (net.minecraft.world.server.ServerWorld) player.level, event.getPos(), null);
                int extraCount = 0;

                for (ItemStack stack : drops) {
                    if (!stack.isEmpty()) {
                        ItemStack copy = stack.copy();
                        copy.setCount(copy.getCount() * 3); // 1 оригинал + 3 = x4
                        player.addItem(copy);
                        extraCount += copy.getCount();
                    }
                }

                if (extraCount > 0) {
                    player.displayClientMessage(new StringTextComponent("§6Рудокоп: Дроп x4!"), true);
                }
            }
            PlayerEventHandler.syncData(player, data);
        });
    }
}