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
        // Скрываем ванильные сердечки, еду, броню и ОПЫТ
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
        PlayerEntity player = mc.player;
        if (player == null) return;

        MatrixStack ms = event.getMatrixStack();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        int centerX = width / 2;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {

            // === ВЕРХНИЙ ЛЕВЫЙ УГОЛ (Уровень, Жизнь, Еда, Мана) ===
            final int leftX = 10;
            final int startY = 10;

            // 1. Текст уровня (слева сверху)
            String levelText = "Уровень: " + data.getLevel();
            mc.font.drawShadow(ms, levelText, leftX, startY, 0xFFFF55);

            // 2. Полоска Жизни
            int hp = (int) player.getHealth();
            int maxHp = (int) player.getMaxHealth();
            drawBar(ms, mc, leftX, startY + 12, 100, 10, (float)hp/maxHp, 0xFF555555, 0xFFCC0000, hp + " / " + maxHp);

            // 3. Полоска Еды
            int food = player.getFoodData().getFoodLevel() * 5;
            drawBar(ms, mc, leftX, startY + 24, 100, 10, (float)food/100f, 0xFF555555, 0xFFFFAA00, food + " / 100");

            // 4. Полоска Маны
            int mana = data.getMana();
            int maxMana = data.getMaxMana();
            drawBar(ms, mc, leftX, startY + 36, 100, 10, (float)mana/maxMana, 0xFF555555, 0xFF0000FF, mana + " / " + maxMana);


            // === НИЗ ПО ЦЕНТРУ (Наша полоска Опыта с текстом) ===
            int currentLevel = data.getLevel();
            int currentXp = data.getXp();
            int xpNeeded = currentLevel * 100;
            float progress = (float) currentXp / xpNeeded;

            int xpBarWidth = 182;
            // ИЗМЕНЕНИЕ ЗДЕСЬ: Прижали полоску к хотбару (height - 32)
            int xpBarY = height - 32;
            int xpBarHeight = 10;

            String xpText = currentXp + " / " + xpNeeded;
            drawBar(ms, mc, centerX - (xpBarWidth / 2), xpBarY, xpBarWidth, xpBarHeight, progress, 0xFF555555, 0xFF00FF00, xpText);

            // === ПРАВЫЙ НИЖНИЙ УГОЛ (Крупная цифра уровня) ===
            String bigLevelText = "" + currentLevel;
            int levelWidth = mc.font.width(bigLevelText);
            int levelX = width - levelWidth - 10;
            // Чуть поднял цифру, чтобы она не наезжала на новую позицию полоски, либо оставил как было
            int levelY = height - 20;

            // Рисуем уровень крупным желтым шрифтом
            mc.font.drawShadow(ms, bigLevelText, levelX, levelY, 0xFFFF55);
        });
    }

    private static void drawBar(MatrixStack ms, Minecraft mc, int x, int y, int width, int height, float progress, int bgColor, int fillColor, String text) {
        progress = Math.max(0.0f, Math.min(1.0f, progress));

        // Фон
        AbstractGui.fill(ms, x, y, x + width, y + height, bgColor);
        // Заполнение
        int fillWidth = (int) (width * progress);
        AbstractGui.fill(ms, x, y, x + fillWidth, y + height, fillColor);

        // Рисуем текст по центру полоски
        if (text != null && !text.isEmpty()) {
            int textWidth = mc.font.width(text);
            // +1 пиксель по Y, чтобы текст был ровно по центру
            mc.font.drawShadow(ms, text, x + (width - textWidth) / 2f, y + 1, 0xFFFFFF);
        }
    }
}