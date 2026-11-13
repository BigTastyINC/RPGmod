package com.example.examplemod.client;

import com.example.examplemod.network.AbandonQuestPacket;
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

    private int currentTab = 0;

    private int listX, listY;

    // НОВОЕ: Список списков. Каждый внутренний список — это один квест, разбитый на строки.
    private final List<List<IReorderingProcessor>> renderedQuests = new ArrayList<>();

    public QuestScreen(List<Quest> availableQuests, List<Quest> activeQuests, int tab) {
        super(new StringTextComponent("Доска Квестов"));
        this.availableQuests.addAll(availableQuests);
        this.activeQuests.addAll(activeQuests);
        this.currentTab = tab;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.buttons.clear();
        this.children.clear();

        listX = centerX - 115; // Начало текста (левая страница)
        listY = centerY - 75;  // Начало списка
        renderedQuests.clear();

        int currentY = listY;

        // Кнопки вкладок
        this.addButton(new Button(centerX - 120, centerY - 115, 80, 20,
                new StringTextComponent(currentTab == 0 ? "§6Доступные" : "§7Доступные"),
                b -> Minecraft.getInstance().setScreen(new QuestScreen(availableQuests, activeQuests, 0))));

        this.addButton(new Button(centerX + 40, centerY - 115, 80, 20,
                new StringTextComponent(currentTab == 1 ? "§eАктивные" : "§7Активные"),
                b -> Minecraft.getInstance().setScreen(new QuestScreen(availableQuests, activeQuests, 1))));

        // Рендер списка
        if (currentTab == 0) {
            for (int i = 0; i < Math.min(4, availableQuests.size()); i++) {
                final Quest quest = availableQuests.get(i);

                // Разбиваем текст на строки шириной 90 пикселей (чтобы не вылезало за рамку)
                List<IReorderingProcessor> lines = this.font.split(quest.getDescription(), 90);
                renderedQuests.add(lines);

                // Кнопка "Принять" (на правой странице)
                this.addButton(new Button(centerX + 20, currentY, 50, 16,
                        new StringTextComponent("§aПринять"),
                        b -> {
                            NetworkHandler.INSTANCE.sendToServer(new AcceptQuestPacket(quest));
                            this.onClose();
                        }));

                // Сдвигаем Y для следующего квеста (высота текста + отступ)
                currentY += Math.max(24, lines.size() * 10 + 12);
            }
        } else {
            for (int i = 0; i < Math.min(4, activeQuests.size()); i++) {
                Quest quest = activeQuests.get(i);

                List<IReorderingProcessor> lines = this.font.split(quest.getDescription(), 90);
                renderedQuests.add(lines);

                // Кнопка "Отказ"
                this.addButton(new Button(centerX + 20, currentY, 45, 16,
                        new StringTextComponent("§cОтказ"),
                        b -> {
                            NetworkHandler.INSTANCE.sendToServer(new AbandonQuestPacket(quest.getId()));
                            this.onClose();
                        }));

                // Кнопка "Сдать"
                if (quest.isCompleted()) {
                    this.addButton(new Button(centerX + 70, currentY, 45, 16,
                            new StringTextComponent("§aСдать"),
                            b -> {
                                NetworkHandler.INSTANCE.sendToServer(new CompleteQuestPacket(quest));
                                this.onClose();
                            }));
                }

                currentY += Math.max(24, lines.size() * 10 + 12);
            }
        }

        this.addButton(new Button(centerX - 40, centerY + 100, 80, 20,
                new StringTextComponent("Закрыть"), b -> this.onClose()));
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Рисуем книгу
        Minecraft.getInstance().getTextureManager().bind(TEXTURE);
        blit(ms, centerX - 256, centerY - 128, 0, 0, 512, 256, 512, 256);

        // Заголовок
        drawCenteredString(ms, this.font, "§6✦ Доска Квестов ✦", centerX, centerY - 125, 0xFFFFFF);

        // Отрисовка текста (построчно для каждого квеста)
        int currentY = listY;
        for (List<IReorderingProcessor> questLines : renderedQuests) {
            for (IReorderingProcessor line : questLines) {
                this.font.drawShadow(ms, line, listX, currentY, 0xFFFFFF);
                currentY += 10; // Высота одной строки
            }
            currentY += 12; // Отступ между квестами
        }

        super.render(ms, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}