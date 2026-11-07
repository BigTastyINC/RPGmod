package com.example.examplemod.client;

import com.example.examplemod.buffs.RpgBuffs;
import com.example.examplemod.capability.ModCapabilities;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = "examplemod", value = Dist.CLIENT)
public class RpgHud extends AbstractGui {

    private static final ResourceLocation HUD_TEXTURE = new ResourceLocation("examplemod", "textures/gui/rpg_hud.png");

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
        if (mc.screen != null) return;

        PlayerEntity player = mc.player;
        if (player == null) return;

        MatrixStack ms = event.getMatrixStack();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        int centerX = width / 2;

        RenderSystem.enableBlend();

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {

            int leftX = 15;
            int startY = 10;

            float scaleX = 1.1f;
            float scaleY = 0.5f;

            int spacing = 3;
            int barHeightOnScreen = (int)(29 * scaleY);

            // ===== 1. ЗДОРОВЬЕ =====
            int hp = (int) player.getHealth();
            int maxHp = (int) player.getMaxHealth();
            drawCustomBar(ms, mc, leftX, startY, scaleX, scaleY, (float)hp/maxHp, 0x77FF5555, 0x77AA0000, "HP: " + hp + " / " + maxHp);

            // ===== 2. СТАМИНА =====
            int stamina = data.getStamina();
            int maxStamina = data.getMaxStamina();
            drawCustomBar(ms, mc, leftX, startY + barHeightOnScreen + spacing, scaleX, scaleY, (float)stamina/maxStamina, 0x77FFDD55, 0x77AA7700, "ST: " + stamina + " / " + maxStamina);

            // ===== 3. МАНА =====
            int mana = data.getMana();
            int maxMana = data.getMaxMana();
            drawCustomBar(ms, mc, leftX, startY + (barHeightOnScreen + spacing) * 2, scaleX, scaleY, (float)mana/maxMana, 0x775555FF, 0x770000AA, "MP: " + mana + " / " + maxMana);

            // ===== 4. ОПЫТ (ОПУЩЕН К ХОТБАРУ) =====
            int currentLevel = data.getLevel();
            int currentXp = data.getXp();
            int xpNeeded = currentLevel * 100;

            String xpText = "§aXP: " + currentXp + " / " + xpNeeded;
            // ИЗМЕНЕНО: height - 32 (прямо над хотбаром)
            mc.font.drawShadow(ms, xpText, centerX - (mc.font.width(xpText) / 2f), height - 32, 0xFFFFFF);

            // ===== УРОВЕНЬ =====
            String levelStr = "Lvl. " + currentLevel;
            mc.font.drawShadow(ms, levelStr, leftX, startY + (barHeightOnScreen + spacing) * 3 + 2, 0xFFFF55);
        });

        RenderSystem.disableBlend();
        renderBuffs(ms, mc, player, width);
    }

    private static void drawCustomBar(MatrixStack ms, Minecraft mc, int x, int y, float scaleX, float scaleY, float progress, int colorTop, int colorBottom, String text) {
        // 1. Текстура
        ms.pushPose();
        ms.translate(x, y, 0);
        ms.scale(scaleX, scaleY, 1.0f);

        mc.getTextureManager().bind(HUD_TEXTURE);
        blit(ms, 0, 0, 0, 0, 158, 29, 256, 256);

        // 2. Градиентная заливка
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        int fillWidth = (int) ((158 - 10) * progress);
        drawGradientFill(ms, 5, 6, fillWidth, 19, colorTop, colorBottom);

        ms.popPose();

        // 3. Текст
        ms.pushPose();
        float textScale = 0.6f;
        float centerX = x + (158 * scaleX) / 2f;
        float centerY = y + (29 * scaleY) / 2f;

        ms.translate(centerX, centerY, 0);
        ms.scale(textScale, textScale, 1.0f);

        int textWidth = mc.font.width(text);
        mc.font.drawShadow(ms, text, -textWidth / 2f, -3, 0xFFFFFF);
        ms.popPose();
    }

    private static void drawGradientFill(MatrixStack ms, int x, int y, int width, int height, int colorTop, int colorBottom) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);

        Matrix4f matrix = ms.last().pose();

        BufferBuilder buffer = Tessellator.getInstance().getBuilder();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);

        int aT = (colorTop >> 24) & 0xFF;
        int rT = (colorTop >> 16) & 0xFF;
        int gT = (colorTop >> 8) & 0xFF;
        int bT = colorTop & 0xFF;

        int aB = (colorBottom >> 24) & 0xFF;
        int rB = (colorBottom >> 16) & 0xFF;
        int gB = (colorBottom >> 8) & 0xFF;
        int bB = colorBottom & 0xFF;

        buffer.vertex(matrix, x + width, y, 0.0f).color(rT, gT, bT, aT).endVertex();
        buffer.vertex(matrix, x, y, 0.0f).color(rT, gT, bT, aT).endVertex();
        buffer.vertex(matrix, x, y + height, 0.0f).color(rB, gB, bB, aB).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0.0f).color(rB, gB, bB, aB).endVertex();

        buffer.end();
        WorldVertexBufferUploader.end(buffer);

        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    private static void renderBuffs(MatrixStack ms, Minecraft mc, PlayerEntity player, int width) {
        Map<Integer, Long> buffs = ClientBuffTracker.getActiveBuffs();
        long now = player.level.getGameTime();
        int y = 10;
        int x = width - 10;

        for (Map.Entry<Integer, Long> entry : buffs.entrySet()) {
            int buffId = entry.getKey();
            long end = entry.getValue();
            if (now >= end) continue;

            long remainingTicks = end - now;
            long totalSeconds = remainingTicks / 20;
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;

            String timeStr = String.format("%02d:%02d", minutes, seconds);
            String text = RpgBuffs.NAMES[buffId] + " " + timeStr;

            int textWidth = mc.font.width(text);
            mc.font.drawShadow(ms, text, x - textWidth, y, 0xFFFFFF);
            fill(ms, x - textWidth, y + 10, x, y + 11, 0xFFAA55FF);
            y += 13;
        }
    }
}