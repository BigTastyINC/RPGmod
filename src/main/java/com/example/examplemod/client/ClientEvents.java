package com.example.examplemod.client;

import com.example.examplemod.item.StaffItem;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.OpenStatsGuiPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "examplemod", value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeyBindings.OPEN_STATS.consumeClick() && Minecraft.getInstance().screen == null) {
            NetworkHandler.INSTANCE.sendToServer(new OpenStatsGuiPacket());
        }
    }

    // ЛКМ с посохом открывает HUD выбора заклинания
    @SubscribeEvent
    public static void onRawMouse(InputEvent.RawMouseEvent event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) return;

        ItemStack held = mc.player.getMainHandItem();
        if (held.getItem() instanceof StaffItem) {
            mc.setScreen(new StaffSpellSelectScreen());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderNameplate(RenderNameplateEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            if (player == Minecraft.getInstance().player) return;

            int level = ClientOtherPlayerData.getLevel(player.getUUID());
            event.setContent(new net.minecraft.util.text.StringTextComponent("§7[Lvl " + level + "] ").append(event.getContent()));
        }
        // Прячем неймлейт моба, если он за блоком
        else if (event.getEntity() instanceof MobEntity) {
            MobEntity mob = (MobEntity) event.getEntity();
            if (mob.hasCustomName()) {
                PlayerEntity player = Minecraft.getInstance().player;
                if (player != null && !isVisible(player, mob)) {
                    // ИСПРАВЛЕНО: RenderNameplateEvent не отменяемый, используем setResult
                    event.setResult(Event.Result.DENY);
                }
            }
        }
    }

    private static boolean isVisible(PlayerEntity player, Entity entity) {
        if (player == null || entity == null) return true;

        if (player.distanceToSqr(entity) > 4096.0D) return false;

        Vector3d start = player.getEyePosition(1.0F);
        Vector3d end = entity.getBoundingBox().getCenter();

        RayTraceResult result = player.level.clip(new RayTraceContext(
                start, end,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                player
        ));

        return result.getType() == RayTraceResult.Type.MISS;
    }
}