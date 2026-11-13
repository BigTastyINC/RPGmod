package com.example.examplemod.event;

import com.example.examplemod.buffs.RpgBuffs;
import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.item.ItemAffix;
import com.example.examplemod.item.ItemStatHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "examplemod")
public class DamageHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntityLiving();
        if (target.level.isClientSide) return;

        if (target instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) target;
            boolean hasDodge = false;
            boolean hasPoison = false;

            Optional<top.theillusivec4.curios.api.type.capability.ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosHelper().getCuriosHandler(player).resolve();
            if (curiosHandler.isPresent()) {
                Map<String, ICurioStacksHandler> curioMap = curiosHandler.get().getCurios();
                for (ICurioStacksHandler stacksHandler : curioMap.values()) {
                    IDynamicStackHandler stacks = stacksHandler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);
                        if (stack.isEmpty()) continue;
                        if (ItemStatHelper.hasAffix(stack, ItemAffix.DODGE)) hasDodge = true;
                        if (ItemStatHelper.hasAffix(stack, ItemAffix.POISON)) hasPoison = true;
                    }
                }
            }

            if (hasDodge && RANDOM.nextFloat() < 0.10f) {
                event.setCanceled(true);
                player.displayClientMessage(new net.minecraft.util.text.StringTextComponent("§bУклонение!"), true);
                return;
            }

            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                if (data.hasBuff(RpgBuffs.DAMAGE_RESIST)) {
                    long end = data.getBuffEnd(RpgBuffs.DAMAGE_RESIST);
                    if (player.level.getGameTime() < end) {
                        float reduce = data.getBuffPower(RpgBuffs.DAMAGE_RESIST);
                        event.setAmount(event.getAmount() * (1.0f - reduce));
                    } else data.clearBuff(RpgBuffs.DAMAGE_RESIST);
                }
            });

            float protectionReduction = 0f;
            float reflectionPercent = 0f;
            for (ItemStack armor : player.inventory.armor) {
                if (armor.isEmpty()) continue;
                if (ItemStatHelper.hasAffix(armor, ItemAffix.PROTECTION_AURA)) protectionReduction += 0.10f;
                if (ItemStatHelper.hasAffix(armor, ItemAffix.REFLECTION)) reflectionPercent += 0.10f;
            }
            if (protectionReduction > 0) event.setAmount(event.getAmount() * (1.0f - Math.min(protectionReduction, 0.5f)));
            if (reflectionPercent > 0f) {
                Entity attacker = event.getSource().getEntity();
                if (attacker instanceof LivingEntity) ((LivingEntity) attacker).hurt(DamageSource.thorns(player), event.getAmount() * reflectionPercent);
            }

            if (hasPoison && RANDOM.nextFloat() < 0.15f) {
                Entity attacker = event.getSource().getEntity();
                if (attacker instanceof LivingEntity) ((LivingEntity) attacker).addEffect(new EffectInstance(Effects.POISON, 100, 1));
            }
        }

        Entity source = event.getSource().getEntity();
        if (source instanceof PlayerEntity) {
            PlayerEntity attacker = (PlayerEntity) source;
            ItemStack weapon = attacker.getMainHandItem();
            if (!ItemStatHelper.isWeapon(weapon)) return;

            float damage = event.getAmount();
            if (ItemStatHelper.hasAffix(weapon, ItemAffix.BLOODLUST)) damage *= 1.15f;

            if (ItemStatHelper.hasSilverBonus(weapon) && target instanceof MonsterEntity) {
                if (target.getType().getRegistryName() != null) {
                    String path = target.getType().getRegistryName().getPath();
                    if (path.contains("skeleton") || path.contains("zombie") || path.contains("wither") || path.contains("phantom") || path.contains("drowned") || path.contains("husk") || path.contains("stray") || path.contains("zoglin") || path.contains("zombie_villager")) {
                        damage *= 1.20f;
                    }
                }
            }

            final int[] luck = {0};
            attacker.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> luck[0] = data.getLuck());
            float critChance = 0.10f + (luck[0] * 0.0005f);

            if (ItemStatHelper.hasAffix(weapon, ItemAffix.CRIT) && RANDOM.nextFloat() < critChance) damage *= 2.0f;
            event.setAmount(damage);

            if (ItemStatHelper.hasAffix(weapon, ItemAffix.BLEEDING) && RANDOM.nextFloat() < 0.15f) target.addEffect(new EffectInstance(Effects.WITHER, 60, 0));
            if (ItemStatHelper.hasAffix(weapon, ItemAffix.FIRE_BLADE)) target.setSecondsOnFire(3);
            if (ItemStatHelper.hasAffix(weapon, ItemAffix.SLOWING) && RANDOM.nextFloat() < 0.20f) target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 40, 1));
            if (ItemStatHelper.hasAffix(weapon, ItemAffix.CRUSHING) && RANDOM.nextFloat() < 0.10f) {
                target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 20, 3));
                target.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 20, 3));
            }
            if (ItemStatHelper.hasAffix(weapon, ItemAffix.VAMPIRISM)) attacker.heal(event.getAmount() * 0.05f);
            if (ItemStatHelper.hasOrichalcumBonus(weapon)) attacker.heal(event.getAmount() * 0.05f);
        }
    }
}