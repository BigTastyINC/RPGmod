package com.example.examplemod.client;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.OpenSpellBookGuiPacket;
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
public class InventorySpellbookButton extends AbstractGui {

    // Используем иконку книги
    private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation("examplemod", "textures/item/spell_book.png");

    @SubscribeEvent
    public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.getGui() instanceof InventoryScreen)) return;
        InventoryScreen inv = (InventoryScreen) event.getGui();
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            // Показываем кнопку только если есть хотя бы 1 заклинание
            if (!data.getKnownSpells().isEmpty()) {
                // Позиция кнопки (справа от инвентаря, как ты обвел)
                int x = inv.getGuiLeft() + 176 + 5;
                int y = inv.getGuiTop() + 20;

                MatrixStack ms = event.getMatrixStack();

                // Рисуем фон кнопки (серый квадрат)
                fill(ms, x, y, x + 20, y + 20, 0xFF8B8B8B);
                fill(ms, x + 1, y + 1, x + 19, y + 19, 0xFF373737);

                // Рисуем саму книгу поверх фона
                Minecraft.getInstance().getTextureManager().bind(BUTTON_TEXTURE);
                blit(ms, x + 2, y + 2, 0, 0, 16, 16, 16, 16);

                // Всплывающая подсказка при наведении
                if (event.getMouseX() >= x && event.getMouseX() < x + 20 &&
                        event.getMouseY() >= y && event.getMouseY() < y + 20) {
                    event.getGui().renderTooltip(ms, new StringTextComponent("§6Книга Заклинаний"), event.getMouseX(), event.getMouseY());
                }
            }
        });
    }

    @SubscribeEvent
    public static void onMouseClick(GuiScreenEvent.MouseClickedEvent.Pre event) {
        if (!(event.getGui() instanceof InventoryScreen)) return;
        InventoryScreen inv = (InventoryScreen) event.getGui();
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            if (!data.getKnownSpells().isEmpty()) {
                int x = inv.getGuiLeft() + 176 + 5;
                int y = inv.getGuiTop() + 20;

                // Если кликнули по кнопке — открываем книгу
                if (event.getMouseX() >= x && event.getMouseX() < x + 20 &&
                        event.getMouseY() >= y && event.getMouseY() < y + 20) {
                    NetworkHandler.INSTANCE.sendToServer(new OpenSpellBookGuiPacket());
                    event.setCanceled(true); // Блокируем дальнейшие клики
                }
            }
        });
    }
}