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
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StatsScreen extends Screen {

    private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation("examplemod", "textures/gui/stats_book.png");
    private final List<Button> plusButtons = new ArrayList<>();

    // Переменные для отложенного тултипа
    private List<ITextComponent> pendingTooltip = null;
    private int pendingMouseX = 0;
    private int pendingMouseY = 0;

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
        // Сбрасываем тултип в начале кадра
        this.pendingTooltip = null;

        this.renderBackground(matrixStack);
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        Minecraft.getInstance().getTextureManager().bind(BOOK_TEXTURE);
        blit(matrixStack, centerX - 256, centerY - 128, 0, 0, 512, 256, 512, 256);

        drawCenteredString(matrixStack, this.font, "§6Характеристики", centerX - 70, centerY - 95, 0xFFFFFF);
        drawCenteredString(matrixStack, this.font, "§6Профессии", centerX + 70, centerY - 95, 0xFFFFFF);

        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                int points = getStatPoints();
                drawCenteredString(matrixStack, this.font, "§eОчки: " + points, centerX - 70, centerY - 86, 0xFFFFFF);

                for (Button b : plusButtons) b.visible = points > 0;

                int textX = centerX - 110;
                int textStartY = centerY - 70;
                int lineHeight = 15;

                drawStatWithTooltip(matrixStack, textX, textStartY, "§cСила: " + data.getStrength(), getStatTooltip("strength"), mouseX, mouseY);
                drawStatWithTooltip(matrixStack, textX, textStartY + lineHeight, "§bЛовкость: " + data.getDexterity(), getStatTooltip("dexterity"), mouseX, mouseY);
                drawStatWithTooltip(matrixStack, textX, textStartY + lineHeight * 2, "§dИнтеллект: " + data.getIntelligence(), getStatTooltip("intelligence"), mouseX, mouseY);
                drawStatWithTooltip(matrixStack, textX, textStartY + lineHeight * 3, "§eУдача: " + data.getLuck(), getStatTooltip("luck"), mouseX, mouseY);
                drawStatWithTooltip(matrixStack, textX, textStartY + lineHeight * 4, "§4Жизнь: " + data.getVitality(), getStatTooltip("vitality"), mouseX, mouseY);
                drawStatWithTooltip(matrixStack, textX, textStartY + lineHeight * 5, "§2Реген HP: " + data.getHealthRegen(), getStatTooltip("healthregen"), mouseX, mouseY);
                drawStatWithTooltip(matrixStack, textX, textStartY + lineHeight * 6, "§3Реген MP: " + data.getManaRegen(), getStatTooltip("manaregen"), mouseX, mouseY);
                drawStatWithTooltip(matrixStack, textX, textStartY + lineHeight * 7, "§6Реген СП: " + data.getStaminaRegen(), getStatTooltip("staminaregen"), mouseX, mouseY);

                matrixStack.pushPose();
                matrixStack.translate(centerX - 110, centerY + 55, 0);
                matrixStack.scale(0.5f, 0.5f, 1.0f);
                String info = "§aУр: " + data.getLevel() + " §7| §aОпыт: " + data.getXp() + " §7| §9МП: " + data.getMana() + " §7| §6СП: " + data.getStamina();
                this.font.drawShadow(matrixStack, info, 0, 0, 0xFFFFFF);
                matrixStack.popPose();

                int rightX = centerX + 30;
                int startY = centerY - 70;
                int spacing = 20;

                drawProfession(matrixStack, data, Profession.SMITH, rightX, startY, 0xFFFFAA00, "§6Кузнец", mouseX, mouseY);
                drawProfession(matrixStack, data, Profession.ALCHEMIST, rightX, startY + spacing, 0xFFAA00AA, "§dАлхимик", mouseX, mouseY);
                drawProfession(matrixStack, data, Profession.FISHERMAN, rightX, startY + spacing * 2, 0xFF00AAAA, "§bРыбак", mouseX, mouseY);
                drawProfession(matrixStack, data, Profession.MINER, rightX, startY + spacing * 3, 0xFFFF5555, "§cРудокоп", mouseX, mouseY);
                drawProfession(matrixStack, data, Profession.GARDENER, rightX, startY + spacing * 4, 0xFF55FF55, "§2Фермер", mouseX, mouseY);
                drawProfession(matrixStack, data, Profession.HUNTER, rightX, startY + spacing * 5, 0xFFAAAAFF, "§9Охотник", mouseX, mouseY);
            });
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        // Отрисовываем отложенный тултип в самом конце, чтобы он был поверх всех элементов
        if (this.pendingTooltip != null) {
            List<IReorderingProcessor> tooltip = this.pendingTooltip.stream()
                    .map(ITextComponent::getVisualOrderText)
                    .collect(Collectors.toList());
            this.renderTooltip(matrixStack, tooltip, this.pendingMouseX, this.pendingMouseY);
        }
    }

    private void drawStatWithTooltip(MatrixStack ms, int x, int y, String text, List<ITextComponent> tooltipLines, int mouseX, int mouseY) {
        drawString(ms, this.font, text, x, y, 0xFFFFFF);

        if (mouseX >= x && mouseX <= x + 100 && mouseY >= y && mouseY <= y + 12) {
            this.pendingTooltip = tooltipLines;
            this.pendingMouseX = mouseX;
            this.pendingMouseY = mouseY;
        }
    }

    private List<ITextComponent> getStatTooltip(String stat) {
        List<ITextComponent> tooltip = new ArrayList<>();
        switch (stat) {
            case "strength":
                tooltip.add(new StringTextComponent("§cСила"));
                tooltip.add(new StringTextComponent("§7Увеличивает урон в ближнем бою."));
                tooltip.add(new StringTextComponent("§7Добавляет +3 к макс. стамине за уровень."));
                break;
            case "dexterity":
                tooltip.add(new StringTextComponent("§bЛовкость"));
                tooltip.add(new StringTextComponent("§7Увеличивает скорость атаки."));
                break;
            case "intelligence":
                tooltip.add(new StringTextComponent("§dИнтеллект"));
                tooltip.add(new StringTextComponent("§7Увеличивает максимальный запас маны."));
                tooltip.add(new StringTextComponent("§7Добавляет +10 к макс. мане за уровень."));
                break;
            case "luck":
                tooltip.add(new StringTextComponent("§eУдача"));
                tooltip.add(new StringTextComponent("§7Повышает шанс критического удара."));
                tooltip.add(new StringTextComponent("§7Влияет на редкость дропа с мобов, крафта"));
                tooltip.add(new StringTextComponent("§7и результатов профессий (Рыбак, Фермер и т.д.)."));
                break;
            case "vitality":
                tooltip.add(new StringTextComponent("§4Жизнь"));
                tooltip.add(new StringTextComponent("§7Увеличивает максимальное здоровье."));
                tooltip.add(new StringTextComponent("§7Добавляет +2 к макс. HP за уровень."));
                break;
            case "healthregen":
                tooltip.add(new StringTextComponent("§2Регенерация HP"));
                tooltip.add(new StringTextComponent("§7Ускоряет естественное восстановление"));
                tooltip.add(new StringTextComponent("§7здоровья со временем."));
                break;
            case "manaregen":
                tooltip.add(new StringTextComponent("§3Регенерация MP"));
                tooltip.add(new StringTextComponent("§7Ускоряет восстановление маны."));
                break;
            case "staminaregen":
                tooltip.add(new StringTextComponent("§6Регенерация СП"));
                tooltip.add(new StringTextComponent("§7Ускоряет восстановление стамины"));
                tooltip.add(new StringTextComponent("§7после совершения атак."));
                break;
        }
        return tooltip;
    }

    private void drawProfession(MatrixStack ms, IPlayerLevel data, Profession prof, int x, int y, int barColor, String title, int mouseX, int mouseY) {
        int lvl = data.getProfessionLevel(prof);
        int xp = data.getProfessionXp(prof);
        int max = lvl * 25 * prof.getXpMultiplier();
        float progress = (lvl >= prof.getMaxLevel()) ? 1.0f : (float) xp / max;

        drawString(ms, this.font, title, x, y, 0xFFFFFF);
        drawString(ms, this.font, "§eур. " + lvl + " §7(" + xp + "/" + max + ")", x, y + 7, 0xFFFFFF);
        drawBar(ms, x, y + 16, 110, 3, progress, barColor);

        if (mouseX >= x && mouseX <= x + 110 && mouseY >= y && mouseY <= y + 20) {
            this.pendingTooltip = getProfessionTooltip(prof);
            this.pendingMouseX = mouseX;
            this.pendingMouseY = mouseY;
        }
    }

    private List<ITextComponent> getProfessionTooltip(Profession prof) {
        List<ITextComponent> tooltip = new ArrayList<>();
        switch (prof) {
            case SMITH:
                tooltip.add(new StringTextComponent("§6Кузнец"));
                tooltip.add(new StringTextComponent("§7Создание оружия и брони."));
                tooltip.add(new StringTextComponent("§7Оружие дает больше опыта, чем броня."));
                tooltip.add(new StringTextComponent("§7Влияет на шанс создать редкую вещь."));
                break;
            case ALCHEMIST:
                tooltip.add(new StringTextComponent("§dАлхимик"));
                tooltip.add(new StringTextComponent("§7Создание зелий, посохов и бижутерии."));
                tooltip.add(new StringTextComponent("§7Влияет на редкость зелий и начальную"));
                tooltip.add(new StringTextComponent("§7энергию создаваемых посохов."));
                break;
            case FISHERMAN:
                tooltip.add(new StringTextComponent("§bРыбак"));
                tooltip.add(new StringTextComponent("§7Ловля рыбы."));
                tooltip.add(new StringTextComponent("§7Влияет на уровень и редкость улова."));
                tooltip.add(new StringTextComponent("§7Дает шанс выловить больше рыбы."));
                break;
            case MINER:
                tooltip.add(new StringTextComponent("§cРудокоп"));
                tooltip.add(new StringTextComponent("§7Добыча руды."));
                tooltip.add(new StringTextComponent("§7Дает шанс на добычу руды в тройном"));
                tooltip.add(new StringTextComponent("§7размере (x4)."));
                break;
            case GARDENER:
                tooltip.add(new StringTextComponent("§2Фермер"));
                tooltip.add(new StringTextComponent("§7Сбор урожая."));
                tooltip.add(new StringTextComponent("§7Влияет на редкость собранных плодов."));
                tooltip.add(new StringTextComponent("§7Дает шанс на дополнительный урожай."));
                break;
            case HUNTER:
                tooltip.add(new StringTextComponent("§9Охотник"));
                tooltip.add(new StringTextComponent("§7Убийство животных."));
                tooltip.add(new StringTextComponent("§7Мясо получает уровень убитого моба."));
                tooltip.add(new StringTextComponent("§7Шанс повысить редкость мяса на 1 ступень."));
                break;
        }
        return tooltip;
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