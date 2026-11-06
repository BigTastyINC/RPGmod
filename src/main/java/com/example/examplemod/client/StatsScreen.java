package com.example.examplemod.client;

import com.example.examplemod.capability.IPlayerLevel;
import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.UpgradeStatPacket;
import com.example.examplemod.profession.Profession;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class StatsScreen extends Screen {

    private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation("examplemod", "textures/gui/stats_book.png");

    private final List<Button> plusButtons = new ArrayList<>();

    public StatsScreen() {
        super(new StringTextComponent("Характеристики"));
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        plusButtons.clear();

        int buttonX = centerX - 35;
        int buttonStartY = centerY - 74;
        int lineHeight = 15;

        plusButtons.add(this.addButton(new Button(buttonX, buttonStartY, 15, 15, new StringTextComponent("+"), b -> sendUpgrade("strength"))));
        plusButtons.add(this.addButton(new Button(buttonX, buttonStartY + lineHeight, 15, 15, new StringTextComponent("+"), b -> sendUpgrade("dexterity"))));
        plusButtons.add(this.addButton(new Button(buttonX, buttonStartY + lineHeight * 2, 15, 15, new StringTextComponent("+"), b -> sendUpgrade("intelligence"))));
        plusButtons.add(this.addButton(new Button(buttonX, buttonStartY + lineHeight * 3, 15, 15, new StringTextComponent("+"), b -> sendUpgrade("luck"))));
        plusButtons.add(this.addButton(new Button(buttonX, buttonStartY + lineHeight * 4, 15, 15, new StringTextComponent("+"), b -> sendUpgrade("vitality"))));
        plusButtons.add(this.addButton(new Button(buttonX, buttonStartY + lineHeight * 5, 15, 15, new StringTextComponent("+"), b -> sendUpgrade("healthregen"))));
        plusButtons.add(this.addButton(new Button(buttonX, buttonStartY + lineHeight * 6, 15, 15, new StringTextComponent("+"), b -> sendUpgrade("manaregen"))));
        plusButtons.add(this.addButton(new Button(buttonX, buttonStartY + lineHeight * 7, 15, 15, new StringTextComponent("+"), b -> sendUpgrade("staminaregen"))));

        this.addButton(new Button(centerX - 40, centerY + 95, 80, 20, new StringTextComponent("Закрыть"), b -> this.onClose()));
    }

    private void sendUpgrade(String stat) {
        NetworkHandler.INSTANCE.sendToServer(new UpgradeStatPacket(stat));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        Minecraft.getInstance().getTextureManager().bind(BOOK_TEXTURE);
        blit(matrixStack, centerX - 256, centerY - 128, 0, 0, 512, 256, 512, 256);

        // Заголовки
        drawCenteredString(matrixStack, this.font, "§6Характеристики", centerX - 70, centerY - 95, 0xFFFFFF);
        drawCenteredString(matrixStack, this.font, "§6Профессии", centerX + 70, centerY - 95, 0xFFFFFF);

        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {

                int points = getStatPoints();
                // Очки подняты чуть выше (centerY - 86)
                drawCenteredString(matrixStack, this.font, "§eОчки: " + points, centerX - 70, centerY - 86, 0xFFFFFF);

                // Прячем плюсики, если очков нет
                for (Button b : plusButtons) {
                    b.visible = points > 0;
                }

                int textX = centerX - 110;
                int textStartY = centerY - 70;
                int lineHeight = 15;

                drawString(matrixStack, this.font, "§cСила: " + data.getStrength(), textX, textStartY, 0xFFFFFF);
                drawString(matrixStack, this.font, "§bЛовкость: " + data.getDexterity(), textX, textStartY + lineHeight, 0xFFFFFF);
                drawString(matrixStack, this.font, "§dИнтеллект: " + data.getIntelligence(), textX, textStartY + lineHeight * 2, 0xFFFFFF);
                drawString(matrixStack, this.font, "§eУдача: " + data.getLuck(), textX, textStartY + lineHeight * 3, 0xFFFFFF);
                drawString(matrixStack, this.font, "§4Жизнь: " + data.getVitality(), textX, textStartY + lineHeight * 4, 0xFFFFFF);
                drawString(matrixStack, this.font, "§2Реген HP: " + data.getHealthRegen(), textX, textStartY + lineHeight * 5, 0xFFFFFF);
                drawString(matrixStack, this.font, "§3Реген MP: " + data.getManaRegen(), textX, textStartY + lineHeight * 6, 0xFFFFFF);
                drawString(matrixStack, this.font, "§6Реген СП: " + data.getStaminaRegen(), textX, textStartY + lineHeight * 7, 0xFFFFFF);

                matrixStack.pushPose();
                matrixStack.translate(centerX - 110, centerY + 55, 0);
                matrixStack.scale(0.5f, 0.5f, 1.0f);

                String info = "§aУр: " + data.getLevel() + " §7| §aОпыт: " + data.getXp() + " §7| §9МП: " + data.getMana() + " §7| §6СП: " + data.getStamina();
                this.font.drawShadow(matrixStack, info, 0, 0, 0xFFFFFF);

                matrixStack.popPose();

                int rightX = centerX + 30;
                int startY = centerY - 70;
                int spacing = 26;

                drawProfession(matrixStack, data, Profession.WEAPONSMITH, rightX, startY, 0xFFFFAA00, "§6Оружейник");
                drawProfession(matrixStack, data, Profession.ARMORSMITH, rightX, startY + spacing, 0xFFAAAAAA, "§7Бронник");
                drawProfession(matrixStack, data, Profession.ALCHEMIST, rightX, startY + spacing * 2, 0xFFAA00AA, "§dАлхимик");
                drawProfession(matrixStack, data, Profession.FISHERMAN, rightX, startY + spacing * 3, 0xFF00AAAA, "§bРыбак");
                drawProfession(matrixStack, data, Profession.MINER, rightX, startY + spacing * 4, 0xFFFF5555, "§cРудокоп");
            });
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void drawProfession(MatrixStack ms, IPlayerLevel data, Profession prof, int x, int y, int barColor, String title) {
        int lvl = data.getProfessionLevel(prof);
        int xp = data.getProfessionXp(prof);
        int max = lvl * 25 * prof.getXpMultiplier();

        drawString(ms, this.font, title, x, y, 0xFFFFFF);
        drawString(ms, this.font, "§eур. " + lvl + " §7(" + xp + "/" + max + ")", x, y + 10, 0xFFFFFF);
        drawBar(ms, x, y + 20, 110, 4, lvl >= 100 ? 1.0f : (float) xp / max, barColor);
    }

    private void drawBar(MatrixStack ms, int x, int y, int width, int height, float progress, int fillColor) {
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        fill(ms, x, y, x + width, y + height, 0xFF555555);
        fill(ms, x, y, x + (int) (width * progress), y + height, fillColor);
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