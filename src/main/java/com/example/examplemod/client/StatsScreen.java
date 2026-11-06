package com.example.examplemod.client;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.UpgradeStatPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class StatsScreen extends Screen {

    public StatsScreen() {
        super(new StringTextComponent("Характеристики"));
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int startY = this.height / 2 - 80;
        int lineHeight = 18;
        int buttonX = centerX + 100;

        // Кнопки для статов
        this.addButton(new Button(buttonX, startY - 2, 20, 18, new StringTextComponent("+"), b -> sendUpgrade("strength")));
        this.addButton(new Button(buttonX, startY + lineHeight - 2, 20, 18, new StringTextComponent("+"), b -> sendUpgrade("dexterity")));
        this.addButton(new Button(buttonX, startY + lineHeight * 2 - 2, 20, 18, new StringTextComponent("+"), b -> sendUpgrade("intelligence")));
        this.addButton(new Button(buttonX, startY + lineHeight * 3 - 2, 20, 18, new StringTextComponent("+"), b -> sendUpgrade("luck")));
        this.addButton(new Button(buttonX, startY + lineHeight * 4 - 2, 20, 18, new StringTextComponent("+"), b -> sendUpgrade("vitality")));
        this.addButton(new Button(buttonX, startY + lineHeight * 5 - 2, 20, 18, new StringTextComponent("+"), b -> sendUpgrade("healthregen")));
        this.addButton(new Button(buttonX, startY + lineHeight * 6 - 2, 20, 18, new StringTextComponent("+"), b -> sendUpgrade("manaregen")));
        this.addButton(new Button(buttonX, startY + lineHeight * 7 - 2, 20, 18, new StringTextComponent("+"), b -> sendUpgrade("staminaregen")));

        // Кнопка «Закрыть» — сдвигаем ниже текста, чтобы не было наложения
        int bottomY = startY + lineHeight * 8 + 20;
        this.addButton(new Button(centerX - 50, bottomY + 45, 100, 20, new StringTextComponent("Закрыть"), b -> this.onClose()));
    }

    private void sendUpgrade(String stat) {
        NetworkHandler.INSTANCE.sendToServer(new UpgradeStatPacket(stat));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        int centerX = this.width / 2;
        int startY = this.height / 2 - 80;
        int lineHeight = 18;

        // Увеличенный фон, чтобы кнопка «Закрыть» влезла
        fill(matrixStack, centerX - 150, this.height / 2 - 130, centerX + 150, this.height / 2 + 180, 0xCC000000);

        // Заголовки
        drawCenteredString(matrixStack, this.font, "§6Характеристики", centerX, this.height / 2 - 120, 0xFFFFFF);
        drawCenteredString(matrixStack, this.font, "§eСвободные очки: " + getStatPoints(), centerX, this.height / 2 - 105, 0xFFFFFF);

        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                int textX = centerX - 130;

                drawString(matrixStack, this.font, "§cСила: " + data.getStrength() + " §7(+урон, +макс.стам)", textX, startY, 0xFFFFFF);
                drawString(matrixStack, this.font, "§bЛовкость: " + data.getDexterity(), textX, startY + lineHeight, 0xFFFFFF);
                drawString(matrixStack, this.font, "§dИнтеллект: " + data.getIntelligence(), textX, startY + lineHeight * 2, 0xFFFFFF);
                drawString(matrixStack, this.font, "§eУдача: " + data.getLuck(), textX, startY + lineHeight * 3, 0xFFFFFF);
                drawString(matrixStack, this.font, "§4Жизнь: " + data.getVitality(), textX, startY + lineHeight * 4, 0xFFFFFF);
                drawString(matrixStack, this.font, "§2Реген Жизни: " + data.getHealthRegen(), textX, startY + lineHeight * 5, 0xFFFFFF);
                drawString(matrixStack, this.font, "§3Реген Маны: " + data.getManaRegen(), textX, startY + lineHeight * 6, 0xFFFFFF);
                drawString(matrixStack, this.font, "§6Реген Стамины: " + data.getStaminaRegen(), textX, startY + lineHeight * 7, 0xFFFFFF);

                // Разделитель
                drawCenteredString(matrixStack, this.font, "§8--------------------", centerX, startY + lineHeight * 8 + 5, 0xFFFFFF);

                // Итоговая информация (Уровень, Мана, Стамина)
                int bottomY = startY + lineHeight * 8 + 20;
                drawString(matrixStack, this.font, "§aУровень: " + data.getLevel() + " | Опыт: " + data.getXp(), textX, bottomY, 0xFFFFFF);
                drawString(matrixStack, this.font, "§9Мана: " + data.getMana() + " / " + data.getMaxMana(), textX, bottomY + 14, 0xFFFFFF);
                drawString(matrixStack, this.font, "§6Стамина: " + data.getStamina() + " / " + data.getMaxStamina(), textX, bottomY + 28, 0xFFFFFF);
            });
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private int getStatPoints() {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            final int[] points = {0};
            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> points[0] = data.getStatPoints());
            return points[0];
        }
        return 0;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}