package com.example.examplemod.event;

import com.example.examplemod.buffs.RpgBuffs;
import com.example.examplemod.capability.ModCapabilities;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "examplemod")
public class OreLuckHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof PlayerEntity)) return;
        PlayerEntity player = event.getPlayer();
        if (player.level.isClientSide) return;
        if (!(event.getWorld() instanceof ServerWorld)) return;

        Block block = event.getState().getBlock();
        if (block.getRegistryName() == null) return;
        if (!block.getRegistryName().getPath().contains("ore")) return;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            if (data.hasBuff(RpgBuffs.ORE_LUCK)) {
                long end = data.getBuffEnd(RpgBuffs.ORE_LUCK);
                if (player.level.getGameTime() < end) {
                    float chance = data.getBuffPower(RpgBuffs.ORE_LUCK);
                    if (RANDOM.nextFloat() < chance) {
                        BlockPos pos = event.getPos();
                        List<ItemStack> drops = Block.getDrops(event.getState(), (ServerWorld) event.getWorld(), pos, null);
                        for (ItemStack drop : drops) {
                            player.addItem(drop.copy());
                        }
                    }
                } else {
                    data.clearBuff(RpgBuffs.ORE_LUCK);
                }
            }
        });
    }
}