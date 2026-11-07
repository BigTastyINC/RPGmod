package com.example.examplemod.event;

import com.example.examplemod.buffs.RpgBuffs;
import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.item.Rarity;
import com.example.examplemod.profession.Profession;
import com.example.examplemod.profession.RarityRoller;
import net.minecraft.block.Block;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "examplemod")
public class ProfessionEventHandler {

    private static final Random RANDOM = new Random();

    // === РЫБАК ===
    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player == null || player.level.isClientSide) return;

        List<ItemStack> drops = event.getDrops();
        if (drops.isEmpty()) return;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            int xpGain = PlayerEventHandler.getProfessionXpWithBonus(player, data, 20);
            data.addProfessionXp(Profession.FISHERMAN, xpGain);

            int lvl = data.getProfessionLevel(Profession.FISHERMAN);
            int fishLevel = Math.max(1, lvl + RANDOM.nextInt(7) - 3);
            Rarity rarity = RarityRoller.roll(lvl);

            boolean hasDoubleCatch = data.hasBuff(RpgBuffs.DOUBLE_CATCH) && player.level.getGameTime() < data.getBuffEnd(RpgBuffs.DOUBLE_CATCH);

            // 1. Применяем NBT к оригинальным дропам
            for (ItemStack stack : drops) {
                if (stack.getItem().isEdible()) {
                    CompoundNBT nbt = stack.getOrCreateTag();
                    nbt.putInt("ItemLevel", fishLevel);
                    nbt.putInt("RarityLevel", rarity.ordinal());
                }
            }

            // 2. Двойной улов (выдаем копию вручную на игрока)
            if (hasDoubleCatch) {
                for (ItemStack stack : drops) {
                    if (stack.getItem().isEdible()) {
                        ItemStack copy = stack.copy();
                        copy.setCount(1);
                        spawnItem(player, copy);
                    }
                }
            }

            // 3. Шанс на x5 улов (выдаем 4 копии вручную)
            float chance = lvl * 0.005f;
            if (player.level.random.nextFloat() < chance) {
                for (ItemStack stack : drops) {
                    if (stack.getItem().isEdible()) {
                        ItemStack copy = stack.copy();
                        copy.setCount(4);
                        spawnItem(player, copy);
                    }
                }
                player.displayClientMessage(new StringTextComponent("§bРыбак: Улов x5!"), true);
            }

            PlayerEventHandler.syncData(player, data);
        });
    }

    // Вспомогательный метод для выдачи предмета на игрока
    private static void spawnItem(PlayerEntity player, ItemStack stack) {
        player.level.addFreshEntity(new ItemEntity(player.level, player.getX(), player.getY() + 1, player.getZ(), stack));
    }

    // === РУДОКОП ===
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player == null || player.level.isClientSide) return;

        Block block = event.getState().getBlock();
        if (block.getRegistryName() == null) return;

        String blockName = block.getRegistryName().getPath();
        if (!blockName.contains("ore")) return;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            int xpGain = PlayerEventHandler.getProfessionXpWithBonus(player, data, 15);
            data.addProfessionXp(Profession.MINER, xpGain);

            int lvl = data.getProfessionLevel(Profession.MINER);
            float chance = lvl * 0.005f;
            if (player.level.random.nextFloat() < chance) {
                List<ItemStack> drops = Block.getDrops(event.getState(), (net.minecraft.world.server.ServerWorld) player.level, event.getPos(), null);
                int extraCount = 0;

                for (ItemStack stack : drops) {
                    if (!stack.isEmpty()) {
                        ItemStack copy = stack.copy();
                        copy.setCount(copy.getCount() * 3);
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