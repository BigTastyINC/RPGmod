package com.example.examplemod.client;

import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.OpenStatsGuiPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "examplemod", value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeyBindings.OPEN_STATS.consumeClick() && Minecraft.getInstance().screen == null) {
            NetworkHandler.INSTANCE.sendToServer(new OpenStatsGuiPacket());
        }
    }

    // НОВОЕ: Отрисовка уровня над ником игрока
    @SubscribeEvent
    public static void onRenderNameplate(RenderNameplateEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            // Не рисуем над собой (опционально)
            if (player == Minecraft.getInstance().player) return;

            int level = ClientOtherPlayerData.getLevel(player.getUUID());

            // Добавляем уровень в начало или конец имени
            event.setContent(new net.minecraft.util.text.StringTextComponent("§7[Lvl " + level + "] ").append(event.getContent()));
        }
    }
}