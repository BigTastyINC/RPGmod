package com.example.examplemod.client;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.spells.Spell;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.StringTextComponent;

import java.util.Map;

public class SpellBookScreen extends Screen {

    private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation("examplemod", "textures/gui/stats_book.png");
    private static final ResourceLocation HUD_TEXTURE = new ResourceLocation("examplemod", "textures/gui/rpg_hud.png");

    public SpellBookScreen() {
        super(new StringTextComponent("Книга Заклинаний"));
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        this.addButton(new Button(centerX - 40, centerY + 95, 80, 20, new StringTextComponent("Закрыть"), b -> this.onClose()));
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Рисуем книгу
        Minecraft.getInstance().getTextureManager().bind(BOOK_TEXTURE);
        blit(ms, centerX - 256, centerY - 128, 0, 0, 512, 256, 512, 256);

        // Заголовки
        drawCenteredString(ms, this.font, "§6Ранг Мага", centerX - 70, centerY - 95, 0xFFFFFF);
        drawCenteredString(ms, this.font, "§6Заклинания", centerX + 70, centerY - 95, 0xFFFFFF);

        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {

                // === ЛЕВАЯ СТРАНИЦА: РАНГИ ===
                int leftX = centerX - 110;
                int startY = centerY - 70;
                int mageRank = data.getMageRank();
                int mageXp = data.getMageXp();

                String[] rankNames = {"Обычный", "Ученик", "Подмастерье", "Маг", "Магистр", "Архимаг"};
                drawString(ms, this.font, "§7Текущий ранг:", leftX, startY, 0xFFFFFF);
                drawString(ms, this.font, "§e" + rankNames[mageRank], leftX, startY + 12, 0xFFFFFF);

                // Рисуем 5 полосок
                for (int i = 0; i < 5; i++) {
                    int barY = startY + 30 + (i * 14);

                    // Цифры 1-5
                    drawString(ms, this.font, "§8" + (i + 1), leftX - 10, barY - 1, 0xFFFFFF);

                    float progress = 0;
                    if (i < mageRank) {
                        progress = 1.0f; // Полностью заполнено
                    } else if (i == mageRank) {
                        int xpNeeded = 500 * (int)Math.pow(2, mageRank);
                        progress = (float) mageXp / xpNeeded; // Текущий опыт
                    }

                    // ИСПРАВЛЕНО: Раздельный масштаб. Делаем полоски тоньше по высоте
                    drawCustomBar(ms, leftX, barY, 0.65f, 0.35f, progress);
                }

                // === ПРАВАЯ СТРАНИЦА: СПИСОК ЗАКЛИНАНИЙ ===
                int rightX = centerX + 30;
                int spellY = centerY - 70;

                if (data.getKnownSpells().isEmpty()) {
                    drawString(ms, this.font, "§7Вы пока не выучили", rightX, spellY, 0xFFFFFF);
                    drawString(ms, this.font, "§7ни одного заклинания...", rightX, spellY + 12, 0xFFFFFF);
                } else {
                    int count = 0;
                    for (Map.Entry<String, Integer> entry : data.getKnownSpells().entrySet()) {
                        if (count >= 7) break;

                        Spell spell = Spell.byId(entry.getKey());
                        int spellLevel = entry.getValue();

                        String color = (mageRank >= spellLevel) ? "§a" : "§c";
                        String line = color + spell.getName() + " §7(" + spellLevel + " ур.)";

                        drawString(ms, this.font, line, rightX, spellY + (count * 14), 0xFFFFFF);

                        count++;
                    }
                }
            });
        }
        super.render(ms, mouseX, mouseY, partialTicks);
    }

    // Метод для рисования красивой трубы
    private void drawCustomBar(MatrixStack ms, int x, int y, float scaleX, float scaleY, float progress) {
        // 1. Рисуем Текстуру Трубы
        ms.pushPose();
        ms.translate(x, y, 0);
        // Теперь у нас разный масштаб по X и Y
        ms.scale(scaleX, scaleY, 1.0f);

        Minecraft.getInstance().getTextureManager().bind(HUD_TEXTURE);
        blit(ms, 0, 0, 0, 0, 158, 29, 256, 256);

        // 2. Рисуем Градиентную Заливку (Фиолетовую)
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        int fillWidth = (int) ((158 - 10) * progress);

        // Верхний цвет - светло-фиолетовый, нижний - темно-фиолетовый
        drawGradientFill(ms, 5, 6, fillWidth, 19, 0x77CC55FF, 0x775500AA);

        ms.popPose();
    }

    // Магия градиента (взята из твоего RpgHud)
    private void drawGradientFill(MatrixStack ms, int x, int y, int width, int height, int colorTop, int colorBottom) {
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

    @Override
    public boolean isPauseScreen() { return false; }
}