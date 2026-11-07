package com.example.examplemod.item;

import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.OpenSpellBookGuiPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class SpellBookItem extends Item {
    public SpellBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!worldIn.isClientSide && playerIn instanceof ServerPlayerEntity) {
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerIn),
                    new OpenSpellBookGuiPacket()
            );
        }
        return ActionResult.success(playerIn.getItemInHand(handIn));
    }

    // Первая линия защиты
    @Override
    public boolean onDroppedByPlayer(ItemStack item, PlayerEntity player) {
        return false;
    }
}