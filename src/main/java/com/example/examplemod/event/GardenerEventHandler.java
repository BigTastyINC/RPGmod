package com.example.examplemod.event;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.item.Rarity;
import com.example.examplemod.profession.Profession;
import com.example.examplemod.profession.RarityRoller;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "examplemod")
public class GardenerEventHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onCropHarvest(BlockEvent.BreakEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player == null || player.level.isClientSide) return;

        BlockState state = event.getState();
        if (!(state.getBlock() instanceof CropsBlock)) return;

        CropsBlock crop = (CropsBlock) state.getBlock();
        if (!crop.isMaxAge(state)) return;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            // 1. Начисляем опыт фермера
            int xpGain = PlayerEventHandler.getProfessionXpWithBonus(player, data, 10);
            data.addProfessionXp(Profession.GARDENER, xpGain);
            int lvl = data.getProfessionLevel(Profession.GARDENER);

            // 2. Шанс на бонусный урожай (только сам плод, без семян)
            float chance = (lvl * 0.003f) + (data.getLuck() * 0.0005f);
            if (RANDOM.nextFloat() < chance) {
                ItemStack bonusDrop = getCropItem(crop);
                if (!bonusDrop.isEmpty()) {
                    Rarity rarity = RarityRoller.roll(lvl, data.getLuck());
                    int itemLevel = Math.max(1, lvl + RANDOM.nextInt(3) - 1);

                    CompoundNBT nbt = bonusDrop.getOrCreateTag();
                    nbt.putInt("ItemLevel", itemLevel);
                    nbt.putInt("RarityLevel", rarity.ordinal());

                    if (!player.inventory.add(bonusDrop)) {
                        player.drop(bonusDrop, false);
                    }
                    player.displayClientMessage(new StringTextComponent("§aФермер: дополнительный урожай!"), true);
                }
            }

            PlayerEventHandler.syncData(player, data);
        });
    }

    // Вспомогательный метод: выдает именно плод, а не семена
    private static ItemStack getCropItem(CropsBlock crop) {
        if (crop == Blocks.WHEAT) return new ItemStack(Items.WHEAT);
        if (crop == Blocks.CARROTS) return new ItemStack(Items.CARROT);
        if (crop == Blocks.POTATOES) return new ItemStack(Items.POTATO);
        if (crop == Blocks.BEETROOTS) return new ItemStack(Items.BEETROOT);
        return ItemStack.EMPTY;
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player.level.isClientSide) return;

        ItemStack stack = event.getItem().getItem();
        if (stack.isEmpty()) return;

        Item item = stack.getItem();

        // Проверяем конкретные ID урожая (семена сюда не входят)
        boolean isCrop = item == Items.WHEAT || item == Items.CARROT
                || item == Items.POTATO || item == Items.BEETROOT;

        if (isCrop) {
            // Если у предмета еще нет NBT, навешиваем редкость
            if (!stack.hasTag() || !stack.getTag().contains("ItemLevel")) {
                player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                    int lvl = data.getProfessionLevel(Profession.GARDENER);
                    Rarity rarity = RarityRoller.roll(lvl, data.getLuck());
                    int itemLevel = Math.max(1, lvl + RANDOM.nextInt(3) - 1);

                    CompoundNBT nbt = stack.getOrCreateTag();
                    nbt.putInt("ItemLevel", itemLevel);
                    nbt.putInt("RarityLevel", rarity.ordinal());

                    // Применяем NBT к предмету на земле
                    event.getItem().setItem(stack);
                });
            }
        }
    }
}