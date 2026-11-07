package com.example.examplemod.item;

import com.example.examplemod.buffs.RpgBuffs;
import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.SyncBuffPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class LuckPotionItem extends Item {
    public LuckPotionItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        if (!worldIn.isClientSide) {
            playerIn.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                long endTime = playerIn.level.getGameTime() + (10 * 60 * 20);
                data.setBuff(RpgBuffs.PROFESSION_LUCK, endTime, 1.5f);

                // ОТПРАВЛЯЕМ ПАКЕТ НА КЛИЕНТ
                if (playerIn instanceof ServerPlayerEntity) {
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerIn),
                            new SyncBuffPacket(RpgBuffs.PROFESSION_LUCK, endTime));
                }

                playerIn.sendMessage(new StringTextComponent("§6Вы выпили зелье Удачи в профессии на 10 минут!"), playerIn.getUUID());

                worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(),
                        SoundEvents.GENERIC_DRINK, SoundCategory.PLAYERS, 1.0f, 1.0f);

                if (!playerIn.abilities.instabuild) {
                    stack.shrink(1);
                }
            });
        }
        return ActionResult.success(stack);
    }
}