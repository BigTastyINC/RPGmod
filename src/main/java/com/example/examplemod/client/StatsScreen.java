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
        int startY = this.height / 2 - 80; // Подняли чуть выше

        // Кнопки для статов
        this.addButton(new Button(centerX + 70, startY, 20, 20, new StringTextComponent("+"), b -> sendUpgrade("strength")));
        this.addButton(new Button(centerX + 70, startY + 20, 20, 20, new StringTextComponent("+"), b -> sendUpgrade("dexterity")));
        this.addButton(new Button(centerX + 70, startY + 40, 20, 20, new StringTextComponent("+"), b -> sendUpgrade("intelligence")));
        this.addButton(new Button(centerX + 70, startY + 60, 20, 20, new StringTextComponent("+"), b -> sendUpgrade("luck")));
        this.addButton(new Button(centerX + 70, startY + 80, 20, 20, new StringTextComponent("+"), b -> sendUpgrade("vitality")));

        // НОВЫЕ КНОПКИ
        this.addButton(new Button(centerX + 70, startY + 100, 20, 20, new StringTextComponent("+"), b -> sendUpgrade("healthregen")));
        this.addButton(new Button(centerX + 70, startY + 120, 20, 20, new StringTextComponent("+"), b -> sendUpgrade("manaregen")));

        // Кнопка закрытия
        this.addButton(new Button(centerX - 50, startY + 165, 100, 20, new StringTextComponent("Закрыть"), b -> this.onClose()));
    }

    private void sendUpgrade(String stat) {
        NetworkHandler.INSTANCE.sendToServer(new UpgradeStatPacket(stat));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        // Фон сделали выше и шире
        fill(matrixStack, this.width / 2 - 120, this.height / 2 - 100, this.width / 2 + 120, this.height / 2 + 110, 0xCC000000);

        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                int centerX = this.width / 2;
                int startY = this.height / 2 - 80;

                drawCenteredString(matrixStack, this.font, "§6Характеристики", centerX, this.height / 2 - 90, 0xFFFFFF);
                drawCenteredString(matrixStack, this.font, "§eОчки: " + data.getStatPoints(), centerX, this.height / 2 - 105, 0xFFFFFF);

                drawString(matrixStack, this.font, "§cСила: " + data.getStrength(), centerX - 100, startY, 0xFFFFFF);
                drawString(matrixStack, this.font, "§bЛовкость: " + data.getDexterity(), centerX - 100, startY + 20, 0xFFFFFF);
                drawString(matrixStack, this.font, "§dИнтеллект: " + data.getIntelligence(), centerX - 100, startY + 40, 0xFFFFFF);
                drawString(matrixStack, this.font, "§eУдача: " + data.getLuck(), centerX - 100, startY + 60, 0xFFFFFF);
                drawString(matrixStack, this.font, "§4Жизнь: " + data.getVitality(), centerX - 100, startY + 80, 0xFFFFFF);

                // Отрисовка новых статов
                drawString(matrixStack, this.font, "§2Реген Жизни: " + data.getHealthRegen(), centerX - 100, startY + 100, 0xFFFFFF);
                drawString(matrixStack, this.font, "§3Реген Маны: " + data.getManaRegen(), centerX - 100, startY + 120, 0xFFFFFF);

                drawString(matrixStack, this.font, "§aУровень: " + data.getLevel() + " | Опыт: " + data.getXp(), centerX - 100, startY + 145, 0xFFFFFF);
                drawString(matrixStack, this.font, "§9Мана: " + data.getMana() + " / " + data.getMaxMana(), centerX - 100, startY + 155, 0xFFFFFF);
            });
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}