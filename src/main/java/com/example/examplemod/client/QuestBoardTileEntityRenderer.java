package com.example.examplemod.client;

import com.example.examplemod.block.QuestBoardBlock;
import com.example.examplemod.block.QuestBoardTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

public class QuestBoardTileEntityRenderer extends TileEntityRenderer<QuestBoardTileEntity> {

    public QuestBoardTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(QuestBoardTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        Minecraft mc = Minecraft.getInstance();

        IBakedModel model = mc.getBlockRenderer().getBlockModel(tileEntityIn.getBlockState());
        if (model == null || model == mc.getModelManager().getMissingModel()) return;

        Direction facing = tileEntityIn.getBlockState().getValue(QuestBoardBlock.FACING);

        // Угол поворота вокруг Y. Модель "лицом" на юг по умолчанию.
        // NORTH -> 180, SOUTH -> 0, EAST -> 90, WEST -> 270
        int angle = 0;
        switch (facing) {
            case NORTH: angle = 180; break;
            case EAST:  angle = 90;  break;
            case WEST:  angle = 270; break;
            case SOUTH:
            default:    angle = 0;   break;
        }

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(angle));
        matrixStackIn.translate(-0.5, -0.5, -0.5);

        mc.getBlockRenderer().getModelRenderer().renderModel(
                matrixStackIn.last(),
                bufferIn.getBuffer(RenderType.cutout()),
                tileEntityIn.getBlockState(),
                model,
                1.0F, 1.0F, 1.0F,
                combinedLightIn,
                combinedOverlayIn,
                EmptyModelData.INSTANCE
        );

        matrixStackIn.popPose();
    }
}