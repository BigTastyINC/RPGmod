package com.example.examplemod.event;

import com.example.examplemod.block.ModBlocks;
import com.example.examplemod.block.QuestBoardBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.village.PointOfInterest;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = "examplemod")
public class VillageQuestBoardSpawner {

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getWorld() instanceof ServerWorld)) return;
        ServerWorld world = (ServerWorld) event.getWorld();
        if (world.dimension() != World.OVERWORLD) return;

        ChunkPos chunkPos = event.getChunk().getPos();

        // Планируем выполнение через 20 тиков (1 секунда).
        // За это время чанк гарантированно станет FULL, и POI-менеджер не будет блокироваться.
        world.getServer().tell(new TickDelayedTask(20, () -> {
            tryPlaceQuestBoard(world, chunkPos);
        }));
    }

    private static void tryPlaceQuestBoard(ServerWorld world, ChunkPos chunkPos) {
        // 1. Ищем колокол (POI "meeting") в чанке
        Optional<PointOfInterest> bellOpt = world.getPoiManager().getInChunk(
                poiType -> poiType == PointOfInterestType.MEETING,
                chunkPos,
                PointOfInterestManager.Status.ANY
        ).findFirst();

        if (!bellOpt.isPresent()) return;
        BlockPos bellPos = bellOpt.get().getPos();

        // 2. Проверяем, нет ли уже доски рядом с колоколом (радиус 8 блоков)
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                for (int dy = -5; dy <= 5; dy++) {
                    if (world.getBlockState(bellPos.offset(dx, dy, dz)).getBlock() == ModBlocks.QUEST_BOARD.get()) {
                        return; // доска уже есть — не дублируем
                    }
                }
            }
        }

        // 3. Ищем подходящее место рядом с колоколом
        BlockPos boardPos = findSuitablePosition(world, bellPos);
        if (boardPos == null) return;

        // 4. Ставим доску лицом на север
        world.setBlock(boardPos,
                ModBlocks.QUEST_BOARD.get().defaultBlockState()
                        .setValue(QuestBoardBlock.FACING, Direction.NORTH),
                3);
    }

    private static BlockPos findSuitablePosition(ServerWorld world, BlockPos bellPos) {
        for (int radius = 2; radius <= 6; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) != radius && Math.abs(dz) != radius) continue;

                    int x = bellPos.getX() + dx;
                    int z = bellPos.getZ() + dz;
                    int y = world.getHeight(Heightmap.Type.WORLD_SURFACE, x, z);

                    BlockPos groundPos = new BlockPos(x, y - 1, z);
                    BlockPos boardPos = new BlockPos(x, y, z);

                    if (world.getBlockState(groundPos).getMaterial().isSolid()
                            && world.getBlockState(boardPos).isAir()
                            && world.getBlockState(boardPos.above()).isAir()) {
                        return boardPos;
                    }
                }
            }
        }
        return null;
    }
}