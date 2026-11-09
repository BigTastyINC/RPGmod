package com.example.examplemod.item;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.spells.Spell;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class SpellTomeItem extends Item {
    public SpellTomeItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        if (!worldIn.isClientSide) {
            CompoundNBT nbt = stack.getOrCreateTag();
            String spellId = nbt.getString("SpellId");
            int spellLevel = nbt.getInt("SpellLevel");

            if (spellId.isEmpty()) {
                playerIn.displayClientMessage(new StringTextComponent("§cЭтот том пуст..."), true);
                return ActionResult.fail(stack);
            }

            playerIn.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                // Проверяем, есть ли у игрока уже выученные заклинания (ДО изучения)
                boolean isFirstSpell = data.getKnownSpells().isEmpty();

                data.learnSpell(spellId, spellLevel);
                Spell spell = Spell.byId(spellId);

                if (isFirstSpell) {
                    // Яркое сообщение для первого заклинания
                    playerIn.displayClientMessage(new StringTextComponent(
                            "§aВы выучили первое заклинание: §e" + spell.getName() + " (Уровень " + spellLevel + ")"
                    ), false);
                    playerIn.displayClientMessage(new StringTextComponent(
                            "§6✦ Откройте инвентарь (§eE§6), чтобы увидеть §lКнигу Заклинаний§r §6в правой панели! ✦"
                    ), false);
                } else {
                    // Обычное сообщение для последующих заклинаний
                    playerIn.displayClientMessage(new StringTextComponent(
                            "§aВы выучили заклинание: §e" + spell.getName() + " (Уровень " + spellLevel + ")"
                    ), false);
                }

                if (!playerIn.abilities.instabuild) {
                    stack.shrink(1);
                }
            });
        }
        return ActionResult.success(stack);
    }
}