package com.example.examplemod.client;

import com.example.examplemod.buffs.RpgBuffs;
import com.example.examplemod.item.Rarity;
import com.example.examplemod.network.BuyItemPacket;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.OpenShopPacket;
import com.example.examplemod.network.SellHeldItemPacket;
import com.example.examplemod.shop.ShopUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;

public class TradeScreen extends Screen {
    private final OpenShopPacket data;

    public TradeScreen(OpenShopPacket data) {
        super(new StringTextComponent("Магазин"));
        this.data = data;
    }

    private static String formatPrice(int copper) {
        if (copper < 100) return copper + "м";
        int s = copper / 100;
        int m = copper % 100;
        if (s < 100) return m > 0 ? s + "с " + m + "м" : s + "с";
        int g = s / 100;
        s = s % 100;
        String res = g + "з";
        if (s > 0) res += " " + s + "с";
        if (m > 0) res += " " + m + "м";
        return res;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int priceButtonY = this.height / 2 + 5;

        for (int i = 0; i < data.prices.size(); i++) {
            final int idx = i;
            final int price = data.prices.get(i);
            int bx = centerX - 200 + i * 85;
            this.addButton(new Button(bx, priceButtonY, 80, 20,
                    new StringTextComponent(formatPrice(price)),
                    b -> NetworkHandler.INSTANCE.sendToServer(
                            new BuyItemPacket(data.items.get(idx), price))));
        }

        this.addButton(new Button(centerX - 100, this.height / 2 + 50, 200, 20,
                new StringTextComponent("§aПродать предмет из руки"), b -> {
            NetworkHandler.INSTANCE.sendToServer(new SellHeldItemPacket());
        }));

        this.addButton(new Button(centerX - 50, this.height / 2 + 110, 100, 20,
                new StringTextComponent("Закрыть"), b -> this.onClose()));
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partial) {
        this.renderBackground(ms);
        fill(ms, this.width / 2 - 220, this.height / 2 - 145, this.width / 2 + 220, this.height / 2 + 145, 0xCC000000);

        int centerX = this.width / 2;

        drawCenteredString(ms, this.font, "§6Магазин", centerX, this.height / 2 - 125, 0xFFFFFF);
        drawCenteredString(ms, this.font,
                "§eБаланс: " + data.gold + "з " + data.silver + "с " + data.copper + "м",
                centerX, this.height / 2 - 105, 0xFFFFFF);

        for (int i = 0; i < data.items.size(); i++) {
            int bx = centerX - 200 + i * 85;
            ItemStack preview = data.items.get(i);
            CompoundNBT nbt = preview.getOrCreateTag();
            int level = Math.max(1, nbt.getInt("ItemLevel"));
            int rarity = Math.min(4, nbt.getInt("RarityLevel"));
            Rarity r = Rarity.values()[rarity];

            fill(ms, bx + 28, this.height / 2 - 75, bx + 48, this.height / 2 - 55, 0xFF8B8B8B);
            fill(ms, bx + 30, this.height / 2 - 73, bx + 46, this.height / 2 - 57, 0xFF373737);

            this.itemRenderer.renderAndDecorateItem(preview, bx + 30, this.height / 2 - 73);

            if (preview.getItem().isEdible()) {
                int buffId = RpgBuffs.getBuffForFood(preview.getItem());
                drawCenteredString(ms, this.font, r.getColor() + r.getName(), bx + 40, this.height / 2 - 42, 0xFFFFFF);
                drawCenteredString(ms, this.font, "§b" + RpgBuffs.NAMES[buffId], bx + 40, this.height / 2 - 28, 0xFFFFFF);
                drawCenteredString(ms, this.font, "§7" + RpgBuffs.DURATION_MINUTES[rarity] + " мин", bx + 40, this.height / 2 - 15, 0xFFFFFF);
            } else {
                drawCenteredString(ms, this.font, "§eУр." + level, bx + 40, this.height / 2 - 42, 0xFFFFFF);
                drawCenteredString(ms, this.font, r.getColor() + r.getName(), bx + 40, this.height / 2 - 28, 0xFFFFFF);
            }
        }

        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack held = player.getMainHandItem();
            String hint;
            if (held.isEmpty()) {
                hint = "§7(в руке пусто)";
            } else if (held.getItem().isEdible()) {
                // Еда — продавать нельзя
                hint = "§cЕду продавать нельзя — только покупать";
            } else if (!held.hasTag() || !held.getTag().contains("ItemLevel")) {
                hint = "§7(у предмета нет уровня — нельзя продать)";
            } else {
                hint = "§7В руке: " + held.getDisplayName().getString() + " → " + formatPrice(ShopUtils.calcSellPrice(held));
            }
            drawCenteredString(ms, this.font, hint, centerX, this.height / 2 + 80, 0xFFFFFF);
        }

        super.render(ms, mouseX, mouseY, partial);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}