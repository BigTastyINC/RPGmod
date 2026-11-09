package com.example.examplemod.client;

import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.OpenStatsGuiPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "examplemod", value = Dist.CLIENT)
public class InventoryStatsbookButton extends AbstractGui {

    private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation("examplemod", "textures/item/stats_book.png");

    @SubscribeEvent
    public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.getGui() instanceof InventoryScreen)) return;
        InventoryScreen inv = (InventoryScreen) event.getGui();
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;

        // Позиция: та же X, что у книги заклинаний, но Y ниже на 25 пикселей
        int x = inv.getGuiLeft() + 176 + 5;
        int y = inv.getGuiTop() + 45;

        MatrixStack ms = event.getMatrixStack();

        // Рисуем фон кнопки (серый квадрат)
        fill(ms, x, y, x + 20, y + 20, 0xFF8B8B8B);
        fill(ms, x + 1, y + 1, x + 19, y + 19, 0xFF373737);

        // Рисуем саму иконку книги
        Minecraft.getInstance().getTextureManager().bind(BUTTON_TEXTURE);
        blit(ms, x + 2, y + 2, 0, 0, 16, 16, 16, 16);

        // Всплывающая подсказка
        if (event.getMouseX() >= x && event.getMouseX() < x + 20 &&
                event.getMouseY() >= y && event.getMouseY() < y + 20) {
            event.getGui().renderTooltip(ms, new StringTextComponent("§6Книга Характеристик"), event.getMouseX(), event.getMouseY());
        }
    }

    @SubscribeEvent
    public static void onMouseClick(GuiScreenEvent.MouseClickedEvent.Pre event) {
        if (!(event.getGui() instanceof InventoryScreen)) return;
        InventoryScreen inv = (InventoryScreen) event.getGui();

        int x = inv.getGuiLeft() + 176 + 5;
        int y = inv.getGuiTop() + 45;

        if (event.getMouseX() >= x && event.getMouseX() < x + 20 &&
                event.getMouseY() >= y && event.getMouseY() < y + 20) {
            // Отправляем пакет на сервер для открытия GUI
            NetworkHandler.INSTANCE.sendToServer(new OpenStatsGuiPacket());
            event.setCanceled(true); // Блокируем дальнейшие клики
        }
    }
}