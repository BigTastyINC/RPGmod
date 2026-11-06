package com.example.examplemod.client;

import com.example.examplemod.capability.ModCapabilities;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "examplemod", value = Dist.CLIENT)
public class RpgHud {

    @SubscribeEvent
    public static void onRenderPre(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HEALTH ||
                event.getType() == RenderGameOverlayEvent.ElementType.FOOD ||
                event.getType() == RenderGameOverlayEvent.ElementType.ARMOR ||
                event.getType() == RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderPost(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) return;

        Minecraft mc = Minecraft.getInstance();

        // ИСПРАВЛЕНО: screen вместо currentScreen
        if (mc.screen != null) return;

        PlayerEntity player = mc.player;
        if (player == null) return;

        MatrixStack ms = event.getMatrixStack();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        int centerX = width / 2;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {

            final int leftX = 10;
            final int startY = 10;

            String levelText = "Уровень: " + data.getLevel();
            mc.font.drawShadow(ms, levelText, leftX, startY, 0xFFFF55);

            int hp = (int) player.getHealth();
            int maxHp = (int) player.getMaxHealth();
            drawBar(ms, mc, leftX, startY + 12, 100, 10, (float)hp/maxHp, 0xFF555555, 0xFFCC0000, hp + " / " + maxHp);

            int stamina = data.getStamina();
            int maxStamina = data.getMaxStamina();
            drawBar(ms, mc, leftX, startY + 24, 100, 10, (float)stamina/maxStamina, 0xFF555555, 0xFFFFAA00, stamina + " / " + maxStamina);

            int mana = data.getMana();
            int maxMana = data.getMaxMana();
            drawBar(ms, mc, leftX, startY + 36, 100, 10, (float)mana/maxMana, 0xFF555555, 0xFF0000FF, mana + " / " + maxMana);

            int currentLevel = data.getLevel();
            int currentXp = data.getXp();
            int xpNeeded = currentLevel * 100;
            float progress = (float) currentXp / xpNeeded;

            int xpBarWidth = 182;
            int xpBarY = height - 32;
            int xpBarHeight = 10;

            String xpText = currentXp + " / " + xpNeeded;
            drawBar(ms, mc, centerX - (xpBarWidth / 2), xpBarY, xpBarWidth, xpBarHeight, progress, 0xFF555555, 0xFF00FF00, xpText);

            String bigLevelText = "" + currentLevel;
            int levelWidth = mc.font.width(bigLevelText);
            int levelX = width - levelWidth - 10;
            int levelY = height - 20;
            mc.font.drawShadow(ms, bigLevelText, levelX, levelY, 0xFFFF55);
        });
    }

    private static void drawBar(MatrixStack ms, Minecraft mc, int x, int y, int width, int height, float progress, int bgColor, int fillColor, String text) {
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        AbstractGui.fill(ms, x, y, x + width, y + height, bgColor);
        int fillWidth = (int) (width * progress);
        AbstractGui.fill(ms, x, y, x + fillWidth, y + height, fillColor);
        if (text != null && !text.isEmpty()) {
            int textWidth = mc.font.width(text);
            mc.font.drawShadow(ms, text, x + (width - textWidth) / 2f, y + 1, 0xFFFFFF);
        }
    }
}