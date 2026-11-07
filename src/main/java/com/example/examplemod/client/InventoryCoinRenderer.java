package com.example.examplemod.client;

import com.example.examplemod.capability.ModCapabilities;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "examplemod", value = Dist.CLIENT)
public class InventoryCoinRenderer {

    private static final ResourceLocation COPPER_ICON = new ResourceLocation("examplemod", "textures/item/copper_coin.png");
    private static final ResourceLocation SILVER_ICON = new ResourceLocation("examplemod", "textures/item/silver_coin.png");
    private static final ResourceLocation GOLD_ICON = new ResourceLocation("examplemod", "textures/item/gold_coin.png");

    @SubscribeEvent
    public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.getGui() instanceof InventoryScreen)) return;

        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        if (player == null) return;

        InventoryScreen inv = (InventoryScreen) event.getGui();
        int left = inv.getGuiLeft();
        int top = inv.getGuiTop();
        MatrixStack ms = event.getMatrixStack();

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            int startX = left + 85;
            int startY = top + 168;

            float scale = 0.5f;
            int spacing = 28;

            // === НОВОЕ: РИСУЕМ ФОН ПОД МОНЕТЫ ===
            int bgX = startX - 4;
            int bgY = startY - 4;
            int bgWidth = 88;
            int bgHeight = 18;

            // Темный фон
            AbstractGui.fill(ms, bgX, bgY, bgX + bgWidth, bgY + bgHeight, 0xFF373737);
            // Светлая рамка (сверху и слева)
            AbstractGui.fill(ms, bgX, bgY, bgX + bgWidth, bgY + 1, 0xFF8B8B8B);
            AbstractGui.fill(ms, bgX, bgY, bgX + 1, bgY + bgHeight, 0xFF8B8B8B);
            // Темная рамка (снизу и справа)
            AbstractGui.fill(ms, bgX, bgY + bgHeight - 1, bgX + bgWidth, bgY + bgHeight, 0xFF000000);
            AbstractGui.fill(ms, bgX + bgWidth - 1, bgY, bgX + bgWidth, bgY + bgHeight, 0xFF000000);

            // === 1. ЗОЛОТО ===
            ms.pushPose();
            ms.translate(startX, startY, 0);
            ms.scale(scale, scale, 1.0f);
            mc.getTextureManager().bind(GOLD_ICON);
            AbstractGui.blit(ms, 0, 0, 0, 0, 16, 16, 16, 16);
            ms.popPose();
            mc.font.drawShadow(ms, "" + data.getGold(), startX + 10, startY, 0xFFFF55);

            // === 2. СЕРЕБРО ===
            ms.pushPose();
            ms.translate(startX + spacing, startY, 0);
            ms.scale(scale, scale, 1.0f);
            mc.getTextureManager().bind(SILVER_ICON);
            AbstractGui.blit(ms, 0, 0, 0, 0, 16, 16, 16, 16);
            ms.popPose();
            mc.font.drawShadow(ms, "" + data.getSilver(), startX + spacing + 10, startY, 0xAAAAAA);

            // === 3. МЕДЬ ===
            ms.pushPose();
            ms.translate(startX + (spacing * 2), startY, 0);
            ms.scale(scale, scale, 1.0f);
            mc.getTextureManager().bind(COPPER_ICON);
            AbstractGui.blit(ms, 0, 0, 0, 0, 16, 16, 16, 16);
            ms.popPose();
            mc.font.drawShadow(ms, "" + data.getCopper(), startX + (spacing * 2) + 10, startY, 0xFFAA00);
        });
    }
}