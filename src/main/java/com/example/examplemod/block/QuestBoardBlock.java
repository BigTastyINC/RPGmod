package com.example.examplemod.block;

import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.OpenQuestBoardPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class QuestBoardBlock extends HorizontalBlock {

    // Явно объявляем свойство FACING (имя "facing")
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Коллизия блока
    private static final VoxelShape SHAPE = VoxelShapes.or(
            Block.box(1, 0, 4, 15, 2, 12),
            Block.box(0, 2, 6, 16, 16, 10)
    );

    public QuestBoardBlock(Properties properties) {
        super(properties);
        // ЯВНО задаём дефолтное состояние с FACING = NORTH
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    // ЯВНО регистрируем FACING в состоянии блока (без этого будет краш)
    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModTileEntities.QUEST_BOARD.get().create();
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isClientSide) {
            NetworkHandler.INSTANCE.sendToServer(new OpenQuestBoardPacket());
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.CONSUME;
    }
}