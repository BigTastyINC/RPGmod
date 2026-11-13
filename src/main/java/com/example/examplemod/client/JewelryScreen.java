package com.example.examplemod.client;

import com.example.examplemod.container.JewelryContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class JewelryScreen extends ContainerScreen<JewelryContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("examplemod", "textures/gui/stats_book.png");

    public JewelryScreen(JewelryContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 8;
        this.inventoryLabelY = 72;
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partial) {
        this.renderBackground(ms);

        // Фон (используем книгу, растянутую как в доске квестов)
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        net.minecraft.client.Minecraft.getInstance().getTextureManager().bind(TEXTURE);
        blit(ms, centerX - 256, centerY - 128, 0, 0, 512, 256, 512, 256);

        // Заголовок
        drawCenteredString(ms, this.font, "§6✦ Бижутерия ✦", centerX, centerY - 105, 0xFFFFFF);

        // Отрисовка слотов
        for (int i = 0; i < 7; i++) {
            int x = centerX - 88 + (i % 3) * 34 + (i == 2 ? 17 : 0);
            int y = centerY - 60 + (i / 3) * 30;
            fill(ms, x - 1, y - 1, x + 17, y + 17, 0xFF373737);
            fill(ms, x - 1, y - 1, x + 17, y, 0xFF8B8B8B);
        }

        // Слоты игрока (3x9 + хотбар)
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                int x = centerX - 88 + c * 18;
                int y = centerY + 20 + r * 18;
                fill(ms, x - 1, y - 1, x + 17, y + 17, 0xFF373737);
            }
        }
        for (int c = 0; c < 9; c++) {
            int x = centerX - 88 + c * 18;
            int y = centerY + 78;
            fill(ms, x - 1, y - 1, x + 17, y + 17, 0xFF373737);
        }

        super.render(ms, mouseX, mouseY, partial);
    }

    @Override
    protected void renderBg(MatrixStack ms, float partial, int x, int y) {}

    @Override
    protected void renderLabels(MatrixStack ms, int x, int y) {}

    @Override
    public boolean isPauseScreen() { return false; }
}