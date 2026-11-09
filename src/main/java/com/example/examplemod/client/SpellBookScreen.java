package com.example.examplemod.client;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.network.CraftScrollPacket;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.spells.Spell;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import java.util.Map;

public class SpellBookScreen extends Screen {

    private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation("examplemod", "textures/gui/stats_book.png");

    public SpellBookScreen() {
        super(new StringTextComponent("Книга Заклинаний"));
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        this.addButton(new Button(centerX - 40, centerY + 95, 80, 20, new StringTextComponent("Закрыть"), b -> this.onClose()));

        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                int count = 0;
                for (Map.Entry<String, Integer> entry : data.getKnownSpells().entrySet()) {
                    if (count >= 7) break;

                    final String spellId = entry.getKey();

                    // ИСПРАВЛЕНО: Кнопка теперь ровно на правой странице
                    int btnX = centerX + 115;
                    int btnY = centerY - 70 + (count * 14) - 2;

                    this.addButton(new Button(btnX, btnY, 50, 12, new StringTextComponent("§aКрафт"), b -> {
                        NetworkHandler.INSTANCE.sendToServer(new CraftScrollPacket(spellId));
                    }));

                    count++;
                }
            });
        }
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        Minecraft.getInstance().getTextureManager().bind(BOOK_TEXTURE);
        blit(ms, centerX - 256, centerY - 128, 0, 0, 512, 256, 512, 256);

        drawCenteredString(ms, this.font, "§6Ранг Мага", centerX - 70, centerY - 95, 0xFFFFFF);
        drawCenteredString(ms, this.font, "§6Заклинания", centerX + 70, centerY - 95, 0xFFFFFF);

        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                int leftX = centerX - 110;
                int startY = centerY - 70;
                int mageRank = data.getMageRank();
                int mageXp = data.getMageXp();

                String[] rankNames = {"Обычный", "Ученик", "Подмастерье", "Маг", "Магистр", "Архимаг"};
                drawString(ms, this.font, "§7Текущий ранг:", leftX, startY, 0xFFFFFF);
                drawString(ms, this.font, "§e" + rankNames[mageRank], leftX, startY + 12, 0xFFFFFF);

                for (int i = 0; i < 5; i++) {
                    int barY = startY + 30 + (i * 14);
                    drawString(ms, this.font, "§8" + (i + 1), leftX - 10, barY - 1, 0xFFFFFF);

                    float progress = 0;
                    if (i < mageRank) progress = 1.0f;
                    else if (i == mageRank) {
                        int xpNeeded = 500 * (int)Math.pow(2, mageRank);
                        progress = (float) mageXp / xpNeeded;
                    }
                    drawCustomBar(ms, leftX, barY, 0.65f, 0.35f, progress);
                }

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

    private void drawCustomBar(MatrixStack ms, int x, int y, float scaleX, float scaleY, float progress) {
        ms.pushPose();
        ms.translate(x, y, 0);
        ms.scale(scaleX, scaleY, 1.0f);

        Minecraft.getInstance().getTextureManager().bind(new ResourceLocation("examplemod", "textures/gui/rpg_hud.png"));
        blit(ms, 0, 0, 0, 0, 158, 29, 256, 256);

        progress = Math.max(0.0f, Math.min(1.0f, progress));
        int fillWidth = (int) ((158 - 10) * progress);
        fill(ms, 5, 6, 5 + fillWidth, 25, 0x77CC55FF);

        ms.popPose();
    }

    @Override
    public boolean isPauseScreen() { return false; }
}