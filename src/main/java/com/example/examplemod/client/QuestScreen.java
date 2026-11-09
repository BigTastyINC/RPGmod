package com.example.examplemod.client;

import com.example.examplemod.network.AcceptQuestPacket;
import com.example.examplemod.network.CompleteQuestPacket;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.quest.Quest;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class QuestScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation("examplemod", "textures/gui/stats_book.png");

    private final List<Quest> availableQuests = new ArrayList<>();
    private final List<Quest> activeQuests = new ArrayList<>();

    private final List<List<IReorderingProcessor>> leftPanelLines = new ArrayList<>();
    private final List<Integer> leftPanelYList = new ArrayList<>();

    private final List<List<IReorderingProcessor>> rightPanelLines = new ArrayList<>();
    private final List<Integer> rightPanelYList = new ArrayList<>();

    // Светло-серый цвет (0xE0E0E0), чтобы читалось на фоне
    private static final int TEXT_COLOR = 0xE0E0E0;

    public QuestScreen(List<Quest> availableQuests, List<Quest> activeQuests) {
        super(new StringTextComponent("Доска Квестов"));
        this.availableQuests.addAll(availableQuests);
        this.activeQuests.addAll(activeQuests);
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // --- ЛЕВАЯ ПАНЕЛЬ ---
        int currentLeftY = centerY - 70;

        for (int i = 0; i < Math.min(3, availableQuests.size()); i++) {
            final Quest quest = availableQuests.get(i);

            List<IReorderingProcessor> lines = this.font.split(quest.getDescription(), 75);
            leftPanelLines.add(lines);
            leftPanelYList.add(currentLeftY);

            this.addButton(new Button(centerX - 35, currentLeftY - 2, 30, 16,
                    new StringTextComponent("§aПринять"),
                    b -> {
                        NetworkHandler.INSTANCE.sendToServer(new AcceptQuestPacket(quest));
                        this.onClose();
                    }));

            currentLeftY += Math.max(22, lines.size() * 10 + 8);
        }

        // --- ПРАВАЯ ПАНЕЛЬ ---
        int currentRightY = centerY - 70;

        for (int i = 0; i < Math.min(3, activeQuests.size()); i++) {
            Quest active = activeQuests.get(i);

            ITextComponent status = active.isCompleted()
                    ? new StringTextComponent(" §a✔")
                    : new StringTextComponent(" §7(" + active.getProgress() + "/" + active.getAmountRequired() + ")");

            // Возвращаем §f (белый) для префикса, чтобы не было слишком темно
            ITextComponent fullText = new StringTextComponent("§f").append(active.getDescription()).append(status);

            List<IReorderingProcessor> lines = this.font.split(fullText, 85);
            rightPanelLines.add(lines);
            rightPanelYList.add(currentRightY);

            if (active.isCompleted()) {
                this.addButton(new Button(centerX + 115, currentRightY - 2, 30, 16,
                        new StringTextComponent("§aСдать"),
                        b -> {
                            NetworkHandler.INSTANCE.sendToServer(new CompleteQuestPacket(active));
                            this.onClose();
                        }));
            }

            currentRightY += Math.max(22, lines.size() * 10 + 8);
        }

        this.addButton(new Button(centerX - 40, centerY + 100, 80, 20,
                new StringTextComponent("Закрыть"), b -> this.onClose()));
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        Minecraft.getInstance().getTextureManager().bind(TEXTURE);
        blit(ms, centerX - 256, centerY - 128, 0, 0, 512, 256, 512, 256);

        // Заголовки (используем TEXT_COLOR для единообразия)
        drawCenteredString(ms, this.font, "§6✦ Доска Квестов ✦", centerX, centerY - 100, TEXT_COLOR);
        drawCenteredString(ms, this.font, "§6Доступные:", centerX - 90, centerY - 80, TEXT_COLOR);
        drawCenteredString(ms, this.font, "§eАктивные:", centerX + 70, centerY - 80, TEXT_COLOR);

        // Левый текст (Светло-серый)
        int leftTextX = centerX - 115;
        for (int i = 0; i < leftPanelLines.size(); i++) {
            List<IReorderingProcessor> lines = leftPanelLines.get(i);
            int rowY = leftPanelYList.get(i);
            for (int j = 0; j < lines.size(); j++) {
                // Используем TEXT_COLOR (0xE0E0E0) для текста
                this.font.drawShadow(ms, lines.get(j), (float) leftTextX, (float) (rowY + (j * 10)), TEXT_COLOR);
            }
        }

        // Правый текст (Светло-серый)
        int rightTextX = centerX + 25;
        for (int i = 0; i < rightPanelLines.size(); i++) {
            List<IReorderingProcessor> lines = rightPanelLines.get(i);
            int rowY = rightPanelYList.get(i);
            for (int j = 0; j < lines.size(); j++) {
                this.font.drawShadow(ms, lines.get(j), (float) rightTextX, (float) (rowY + (j * 10)), TEXT_COLOR);
            }
        }

        super.render(ms, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}