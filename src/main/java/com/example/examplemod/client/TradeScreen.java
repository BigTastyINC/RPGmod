package com.example.examplemod.client;

import com.example.examplemod.container.TradeContainer;
import com.example.examplemod.item.Rarity;
import com.example.examplemod.network.BuyItemPacket;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.SellSlotsPacket;
import com.example.examplemod.shop.ShopUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class TradeScreen extends ContainerScreen<TradeContainer> {

    public TradeScreen(TradeContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        this.imageWidth = 520;
        this.imageHeight = 320;
    }

    @Override
    protected void init() {
        super.init();
        int left = this.leftPos;
        int top = this.topPos;

        // Кнопки покупки
        for (int i = 0; i < this.menu.prices.size(); i++) {
            final int idx = i;
            final int price = this.menu.prices.get(i);
            int bx = left + 35 + i * 90;
            this.addButton(new Button(bx, top + 100, 80, 20,
                    new StringTextComponent("§e" + ShopUtils.formatPrice(price)),
                    b -> NetworkHandler.INSTANCE.sendToServer(
                            new BuyItemPacket(this.menu.items.get(idx), price))));
        }

        // Кнопка "Продать всё"
        this.addButton(new Button(left + 130, top + 270, 110, 20,
                new StringTextComponent("§aПродать всё"), b -> {
            NetworkHandler.INSTANCE.sendToServer(new SellSlotsPacket());
        }));

        // Кнопка "Закрыть"
        this.addButton(new Button(left + 280, top + 270, 110, 20,
                new StringTextComponent("Закрыть"), b -> this.onClose()));
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partial) {
        this.renderBackground(ms);

        int left = this.leftPos;
        int top = this.topPos;

        // 1. Основной фон магазина
        fill(ms, left, top, left + 520, top + 320, 0xEE1a1a1a);

        // 2. Товары
        for (int i = 0; i < this.menu.items.size(); i++) {
            int bx = left + 35 + i * 90;
            ItemStack preview = this.menu.items.get(i);
            CompoundNBT nbt = preview.getOrCreateTag();
            int level = Math.max(1, nbt.getInt("ItemLevel"));
            int rarity = Math.min(4, nbt.getInt("RarityLevel"));
            Rarity r = Rarity.values()[rarity];

            // Рамка товара
            fill(ms, bx + 24, top + 40, bx + 56, top + 72, 0xFF333333);
            fill(ms, bx + 25, top + 41, bx + 55, top + 71, 0xFF111111);

            // ИКОНКА
            this.itemRenderer.renderAndDecorateItem(preview, bx + 32, top + 48);

            // Название и уровень
            drawCenteredString(ms, this.font, "§eУр." + level, bx + 40, top + 15, 0xFFFFFF);
            drawCenteredString(ms, this.font, r.getColor() + r.getName(), bx + 40, top + 25, 0xFFFFFF);
        }

        // 3. Заголовок
        drawCenteredString(ms, this.font, "§6✦ МАГАЗИН ✦", left + 260, top - 12, 0xFFFFFF);

        // 4. Слоты продажи
        drawCenteredString(ms, this.font, "§7Положи предметы в слоты для продажи:", left + 260, top + 125, 0xFFFFFF);
        for (int i = 0; i < 10; i++) {
            drawSlotBg(ms, left + 169 + i * 18, top + 139);
        }

        // 5. Инвентарь игрока
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                drawSlotBg(ms, left + 178 + c * 18, top + 179 + r * 18);
            }
        }
        for (int c = 0; c < 9; c++) {
            drawSlotBg(ms, left + 178 + c * 18, top + 237);
        }

        // === НОВОЕ: ПОДСЧЕТ СУММЫ ПРОДАЖИ ===
        int totalSellPrice = 0;
        for (int i = 0; i < 10; i++) {
            ItemStack stack = this.menu.getSlot(i).getItem();
            // Проверяем, что предмет есть и у него есть уровень (как в фильтре слота)
            if (!stack.isEmpty() && stack.hasTag() && stack.getTag().contains("ItemLevel")) {
                totalSellPrice += ShopUtils.calcSellPrice(stack) * stack.getCount();
            }
        }

        // Выводим текст над кнопкой "Продать всё", если есть что продавать
        if (totalSellPrice > 0) {
            String sellText = "§aВы получите: §6" + ShopUtils.formatPrice(totalSellPrice);
            drawCenteredString(ms, this.font, sellText, left + 185, top + 255, 0xFFFFFF);
        }

        // 6. Баланс
        String balance = "§eБаланс: §6" + this.menu.gold + "з §7" + this.menu.silver + "с §e" + this.menu.copper + "м";
        drawCenteredString(ms, this.font, balance, left + 260, top + 300, 0xFFFFFF);

        super.render(ms, mouseX, mouseY, partial);
    }

    private void drawSlotBg(MatrixStack ms, int x, int y) {
        fill(ms, x, y, x + 18, y + 18, 0xFF373737);
        fill(ms, x, y, x + 18, y + 1, 0xFF8B8B8B);
        fill(ms, x, y, x + 1, y + 18, 0xFF8B8B8B);
        fill(ms, x, y + 17, x + 18, y + 18, 0xFF000000);
        fill(ms, x + 17, y, x + 18, y + 18, 0xFF000000);
    }

    @Override
    protected void renderBg(MatrixStack ms, float partialTicks, int x, int y) {}

    @Override
    protected void renderLabels(MatrixStack matrixStack, int x, int y) {}

    @Override
    public boolean isPauseScreen() { return false; }
}