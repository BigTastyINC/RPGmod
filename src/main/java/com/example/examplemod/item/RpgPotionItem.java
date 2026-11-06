package com.example.examplemod.item;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.event.PlayerEventHandler;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.SyncLevelPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class RpgPotionItem extends Item {
    private final boolean isMana;

    public RpgPotionItem(boolean isMana, Properties properties) {
        super(properties);
        this.isMana = isMana;
    }

    public boolean isManaPotion() {
        return isMana;
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        if (!worldIn.isClientSide) {
            CompoundNBT nbt = stack.getOrCreateTag();
            int level = Math.max(1, nbt.getInt("ItemLevel"));
            int rarity = Math.min(4, nbt.getInt("RarityLevel"));

            // Формула: Уровень * 5 * Множитель Редкости
            int amount = (int) (level * 5 * Rarity.values()[rarity].getMultiplier());

            if (isMana) {
                playerIn.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                    data.setMana(data.getMana() + amount);
                    playerIn.sendMessage(new StringTextComponent("§bВосстановлено " + amount + " маны!"), playerIn.getUUID());

                    if (playerIn instanceof ServerPlayerEntity) {
                        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerIn),
                                new SyncLevelPacket(data.getLevel(), data.getXp(), data.getStatPoints(),
                                        data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(),
                                        data.getVitality(), data.getMana(), data.getHealthRegen(), data.getManaRegen(),
                                        data.getStamina(), data.getCopper(), data.getSilver(), data.getGold()));
                    }
                });
            } else {
                playerIn.heal(amount);
                playerIn.sendMessage(new StringTextComponent("§cВосстановлено " + amount + " здоровья!"), playerIn.getUUID());
            }

            worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(),
                    SoundEvents.GENERIC_DRINK, SoundCategory.PLAYERS, 1.0f, 1.0f);

            if (!playerIn.abilities.instabuild) {
                stack.shrink(1);
            }
        }
        return ActionResult.success(stack);
    }
}