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
            // Координаты в левом нижнем углу инвентаря
            int startX = left + 10;
            int startY = top + 166 + 5;

            int iconSize = 6; // Уменьшили иконку в 3 раза (было 16)
            int spacing = 30; // Расстояние между монетками

            // === МЕДЬ ===
            mc.getTextureManager().bind(COPPER_ICON);
            AbstractGui.blit(ms, startX, startY, 0, 0, iconSize, iconSize, 16, 16);
            // Сдвигаем текст вправо на размер иконки + 2 пикселя и чуть вверх для центрирования
            mc.font.drawShadow(ms, "" + data.getCopper(), startX + iconSize + 2, startY - 1, 0xFFAA00);

            // === СЕРЕБРО ===
            mc.getTextureManager().bind(SILVER_ICON);
            AbstractGui.blit(ms, startX + spacing, startY, 0, 0, iconSize, iconSize, 16, 16);
            mc.font.drawShadow(ms, "" + data.getSilver(), startX + spacing + iconSize + 2, startY - 1, 0xAAAAAA);

            // === ЗОЛОТО ===
            mc.getTextureManager().bind(GOLD_ICON);
            AbstractGui.blit(ms, startX + (spacing * 2), startY, 0, 0, iconSize, iconSize, 16, 16);
            mc.font.drawShadow(ms, "" + data.getGold(), startX + (spacing * 2) + iconSize + 2, startY - 1, 0xFFFF55);
        });
    }
}