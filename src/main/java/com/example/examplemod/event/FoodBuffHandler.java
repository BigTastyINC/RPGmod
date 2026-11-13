package com.example.examplemod.event;

import com.example.examplemod.buffs.RpgBuffs;
import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.SyncLevelPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "examplemod")
public class FoodBuffHandler {

    private static final Random RANDOM = new Random();

    private static int rollRarity() {
        int roll = RANDOM.nextInt(100);
        if (roll < 40) return 0;
        if (roll < 70) return 1;
        if (roll < 88) return 2;
        if (roll < 97) return 3;
        return 4;
    }

    public static boolean isPreparedFood(Item item) {
        return item == Items.BREAD
                || item == Items.COOKIE
                || item == Items.BAKED_POTATO
                || item == Items.PUMPKIN_PIE
                || item == Items.MUSHROOM_STEW
                || item == Items.BEETROOT_SOUP
                || item == Items.RABBIT_STEW
                || item == Items.SUSPICIOUS_STEW
                || item == Items.COOKED_BEEF
                || item == Items.COOKED_PORKCHOP
                || item == Items.COOKED_MUTTON
                || item == Items.COOKED_CHICKEN
                || item == Items.COOKED_RABBIT
                || item == Items.COOKED_COD
                || item == Items.COOKED_SALMON
                || item == Items.DRIED_KELP
                || item == Items.GOLDEN_CARROT
                || item == Items.GOLDEN_APPLE
                || item == Items.ENCHANTED_GOLDEN_APPLE
                || item == Items.HONEY_BOTTLE;
    }

    public static void assignRarityIfFood(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (stack.hasTag() && stack.getTag().contains("RarityLevel")) return;
        if (!isPreparedFood(stack.getItem())) return;

        CompoundNBT nbt = stack.getOrCreateTag();
        nbt.putInt("RarityLevel", rollRarity());
    }

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getPlayer().level.isClientSide) return;
        assignRarityIfFood(event.getCrafting());
    }

    @SubscribeEvent
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntityLiving() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
        if (player.level.isClientSide) return;

        ItemStack stack = event.getItem();

        // === Обычное яблоко — +5 HP и +5 маны ===
        if (stack.getItem() == Items.APPLE) {
            player.heal(5.0f);
            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                data.setMana(data.getMana() + 5);
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncLevelPacket(
                                data.getLevel(), data.getXp(), data.getStatPoints(),
                                data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(), data.getVitality(),
                                data.getMana(), data.getHealthRegen(), data.getManaRegen(),
                                data.getStamina(), data.getStaminaRegen(),
                                data.getCopper(), data.getSilver(), data.getGold())
                );
            });
            player.sendMessage(new StringTextComponent("§a+5 HP, §b+5 маны"), player.getUUID());
            return;
        }

        if (!isPreparedFood(stack.getItem())) return;

        assignRarityIfFood(stack);

        int rarity = stack.hasTag() && stack.getTag().contains("RarityLevel")
                ? Math.max(0, Math.min(4, stack.getTag().getInt("RarityLevel")))
                : 0;

        int buffId = RpgBuffs.getBuffForFood(stack.getItem());

        int itemLevel = stack.hasTag() && stack.getTag().contains("ItemLevel")
                ? stack.getTag().getInt("ItemLevel")
                : 1;
        int bonusMinutes = itemLevel / 40;

        final float basePower = RpgBuffs.BASE_POWER[buffId] * RpgBuffs.RARITY_MULTIPLIER[rarity];
        final int totalMinutes = RpgBuffs.DURATION_MINUTES[rarity] + bonusMinutes;
        final int durationTicks = totalMinutes * 60 * 20;
        final long endTime = player.level.getGameTime() + durationTicks;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            float actualPower = basePower;
            long actualEndTime = endTime;

            if (data.hasBuff(buffId) && player.level.getGameTime() < data.getBuffEnd(buffId)) {
                float oldPower = data.getBuffPower(buffId);
                long oldEnd = data.getBuffEnd(buffId);
                if (oldPower > actualPower) actualPower = oldPower;
                if (oldEnd > actualEndTime) actualEndTime = oldEnd;
            }

            data.setBuff(buffId, actualEndTime, actualPower);
        });

        player.sendMessage(new StringTextComponent(
                        "§6Еда дала бафф: §e" + RpgBuffs.NAMES[buffId]
                                + " §7(" + totalMinutes + " мин)"),
                player.getUUID());
    }
}