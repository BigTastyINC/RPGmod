package com.example.examplemod.item;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.entity.ModEntities;
import com.example.examplemod.entity.RpgFireballEntity;
import com.example.examplemod.entity.RpgFrostboltEntity;
import com.example.examplemod.entity.SummonedSkeletonArcher;
import com.example.examplemod.entity.SummonedSkeletonWarrior;
import com.example.examplemod.entity.SummonedZombie;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.SyncLevelPacket;
import com.example.examplemod.spells.Spell;
import net.minecraft.block.BlockState;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpellScrollItem extends Item {
    public SpellScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        if (!worldIn.isClientSide) {
            CompoundNBT nbt = stack.getOrCreateTag();
            if (!nbt.contains("SpellId")) {
                playerIn.displayClientMessage(new StringTextComponent("§cЭтот свиток пуст..."), true);
                return ActionResult.fail(stack);
            }

            String spellId = nbt.getString("SpellId");
            Spell spell = Spell.byId(spellId);
            int spellLevel = nbt.getInt("SpellLevel");

            playerIn.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                int manaCost = spell.getManaCost(data.getMageRank());

                if (data.getMana() < manaCost) {
                    playerIn.displayClientMessage(new StringTextComponent("§cНедостаточно маны! Требуется: " + manaCost), true);
                    return;
                }

                int rarityIndex = Math.min(4, nbt.getInt("RarityLevel"));
                float finalDamage = spell.getDamage(spellLevel, rarityIndex);
                Vector3d look = playerIn.getLookAngle();

                if (spellId.equals("teleport_ore")) {
                    int radius = 40;
                    List<BlockPos> ores = findNearestOres(playerIn, radius, 3);

                    if (!ores.isEmpty()) {
                        data.setMana(data.getMana() - manaCost);
                        data.addMageXp(10 + (spellLevel * 5));

                        int count = 0;
                        for (BlockPos pos : ores) {
                            BlockState state = worldIn.getBlockState(pos);
                            worldIn.removeBlock(pos, false);
                            ItemStack drop = new ItemStack(state.getBlock().asItem());
                            if (!playerIn.inventory.add(drop)) playerIn.drop(drop, false);
                            count++;
                        }

                        playerIn.displayClientMessage(new StringTextComponent("§bВы притянули §e" + count + " §bблоков руды!"), false);
                        worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                        if (!playerIn.abilities.instabuild) stack.shrink(1);
                        sync(playerIn, data);
                    } else {
                        playerIn.displayClientMessage(new StringTextComponent("§cВ радиусе " + radius + " блоков не найдено руды!"), true);
                    }
                    return;
                }

                if (spellId.equals("craft_relic")) {
                    data.setMana(data.getMana() - manaCost);
                    data.addMageXp(10 + (spellLevel * 5));

                    ItemStack relic = new ItemStack(Items.DIAMOND);
                    CompoundNBT relicNbt = relic.getOrCreateTag();
                    relicNbt.putInt("ItemLevel", data.getLevel());
                    int r = playerIn.getRandom().nextInt(5);
                    relicNbt.putInt("RarityLevel", r);
                    relicNbt.putInt("RelicBasePrice", 100000 + playerIn.getRandom().nextInt(1900001));

                    if (!playerIn.inventory.add(relic)) playerIn.drop(relic, false);

                    playerIn.displayClientMessage(new StringTextComponent("§aВы создали реликвию!"), false);
                    worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.ANVIL_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    if (!playerIn.abilities.instabuild) stack.shrink(1);
                    sync(playerIn, data);
                    return;
                }

                if (spellId.equals("summon_merchant")) {
                    data.setMana(data.getMana() - manaCost);
                    data.addMageXp(10 + (spellLevel * 5));

                    VillagerEntity npc = new VillagerEntity(net.minecraft.entity.EntityType.VILLAGER, worldIn);
                    npc.setPos(playerIn.getX() + 2, playerIn.getY(), playerIn.getZ() + 2);
                    npc.setCustomName(new StringTextComponent("§5Маг"));
                    npc.setVillagerData(npc.getVillagerData().setProfession(VillagerProfession.CLERIC));
                    npc.setCustomNameVisible(true);
                    npc.setPersistenceRequired();
                    npc.getPersistentData().putBoolean("IsSummonedMerchant", true);
                    npc.getPersistentData().putLong("SummonTime", worldIn.getGameTime());
                    worldIn.addFreshEntity(npc);

                    playerIn.displayClientMessage(new StringTextComponent("§aПризван Торговец-Маг! §7(Исчезнет через 5 минут)"), false);
                    worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    if (!playerIn.abilities.instabuild) stack.shrink(1);
                    sync(playerIn, data);
                    return;
                }

                // Призыв нежити (свиток)
                if (spellId.equals("summon_undead")) {
                    // Проверка ранга (раньше её тут не было!)
                    if (data.getMageRank() < spellLevel) {
                        playerIn.displayClientMessage(new StringTextComponent("§cВаш ранг магии слишком низок! Требуется: " + spellLevel), true);
                        return;
                    }

                    float requiredHp = playerIn.getMaxHealth() * 0.3f;
                    if (playerIn.getHealth() < requiredHp) {
                        playerIn.displayClientMessage(new StringTextComponent("§cНедостаточно здоровья! Требуется: " + (int) requiredHp), true);
                        return;
                    }

                    data.setMana(data.getMana() - manaCost);
                    data.addMageXp(10 + (spellLevel * 5));
                    playerIn.setHealth(playerIn.getHealth() - requiredHp);

                    int rank = data.getMageRank();
                    int archers = 0, warriors = 0, zombies = 0;

                    switch (rank) {
                        case 1: archers = 1; warriors = 1; break;
                        case 2: archers = 1; warriors = 1; zombies = 1; break;
                        case 3: archers = 2; warriors = 1; zombies = 1; break;
                        case 4: archers = 2; warriors = 2; zombies = 1; break;
                        case 5: archers = 2; warriors = 2; zombies = 1; break;
                        default: archers = 1; break; // На 0 ранге хотя бы 1 лучник
                    }

                    int spawned = 0;
                    Random rand = playerIn.getRandom();
                    long summonTime = worldIn.getGameTime();

                    // Отладка
                    playerIn.displayClientMessage(new StringTextComponent("§7[DEBUG] Rage=" + rank + ", A=" + archers + ", W=" + warriors + ", Z=" + zombies), false);

                    for (int i = 0; i < archers; i++) {
                        SummonedSkeletonArcher s = new SummonedSkeletonArcher(ModEntities.SUMMONED_SKELETON_ARCHER.get(), worldIn);
                        s.setPos(playerIn.getX() + (rand.nextFloat() - 0.5f) * 6, playerIn.getY(), playerIn.getZ() + (rand.nextFloat() - 0.5f) * 6);
                        s.setOwnerUUID(playerIn.getUUID());
                        s.applyOwnerStats(playerIn);
                        s.getPersistentData().putLong("SummonTime", summonTime);
                        if (worldIn.addFreshEntity(s)) spawned++;
                    }

                    for (int i = 0; i < warriors; i++) {
                        SummonedSkeletonWarrior s = new SummonedSkeletonWarrior(ModEntities.SUMMONED_SKELETON_WARRIOR.get(), worldIn);
                        s.setPos(playerIn.getX() + (rand.nextFloat() - 0.5f) * 6, playerIn.getY(), playerIn.getZ() + (rand.nextFloat() - 0.5f) * 6);
                        s.setOwnerUUID(playerIn.getUUID());
                        s.applyOwnerStats(playerIn);
                        s.getPersistentData().putLong("SummonTime", summonTime);
                        if (rank == 5) {
                            s.setItemSlot(EquipmentSlotType.HEAD, new ItemStack(Items.LEATHER_HELMET));
                            s.setItemSlot(EquipmentSlotType.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
                            s.setItemSlot(EquipmentSlotType.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
                            s.setItemSlot(EquipmentSlotType.FEET, new ItemStack(Items.LEATHER_BOOTS));
                        }
                        if (worldIn.addFreshEntity(s)) spawned++;
                    }

                    for (int i = 0; i < zombies; i++) {
                        SummonedZombie z = new SummonedZombie(ModEntities.SUMMONED_ZOMBIE.get(), worldIn);
                        z.setPos(playerIn.getX() + (rand.nextFloat() - 0.5f) * 6, playerIn.getY(), playerIn.getZ() + (rand.nextFloat() - 0.5f) * 6);
                        z.setOwnerUUID(playerIn.getUUID());
                        z.applyOwnerStats(playerIn);
                        z.getPersistentData().putLong("SummonTime", summonTime);
                        if (rank == 5) {
                            z.setItemSlot(EquipmentSlotType.HEAD, new ItemStack(Items.LEATHER_HELMET));
                            z.setItemSlot(EquipmentSlotType.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
                            z.setItemSlot(EquipmentSlotType.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
                            z.setItemSlot(EquipmentSlotType.FEET, new ItemStack(Items.LEATHER_BOOTS));
                        }
                        if (worldIn.addFreshEntity(z)) spawned++;
                    }

                    playerIn.displayClientMessage(new StringTextComponent("§aПризвано: §e" + spawned + " §aнежити! §7(-" + (int) requiredHp + " HP)"), false);
                    worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    if (!playerIn.abilities.instabuild) stack.shrink(1);
                    sync(playerIn, data);
                    return;
                }

                // Стандартные заклинания
                data.setMana(data.getMana() - manaCost);
                data.addMageXp(10 + (spellLevel * 5));

                playerIn.displayClientMessage(new StringTextComponent("§dВы использовали свиток: §e" + spell.getName() + " §7(Урон: " + (int) finalDamage + ", -" + manaCost + " маны)"), false);

                if (spellId.equals("heal")) {
                    playerIn.heal(finalDamage);
                } else if (spellId.equals("fireball")) {
                    RpgFireballEntity fireball = new RpgFireballEntity(worldIn, playerIn, look.x, look.y, look.z, finalDamage);
                    worldIn.addFreshEntity(fireball);
                } else if (spellId.equals("frostbolt")) {
                    RpgFrostboltEntity frostbolt = new RpgFrostboltEntity(worldIn, playerIn, look.x, look.y, look.z, finalDamage);
                    worldIn.addFreshEntity(frostbolt);
                }

                worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                if (!playerIn.abilities.instabuild) stack.shrink(1);
                sync(playerIn, data);
            });
        }
        return ActionResult.success(stack);
    }

    private void sync(PlayerEntity player, com.example.examplemod.capability.IPlayerLevel data) {
        if (player instanceof ServerPlayerEntity) {
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                    new SyncLevelPacket(data.getLevel(), data.getXp(), data.getStatPoints(),
                            data.getStrength(), data.getDexterity(), data.getIntelligence(), data.getLuck(), data.getVitality(),
                            data.getMana(), data.getHealthRegen(), data.getManaRegen(),
                            data.getStamina(), data.getStaminaRegen(),
                            data.getCopper(), data.getSilver(), data.getGold()));
        }
    }

    private List<BlockPos> findNearestOres(PlayerEntity player, int radius, int maxCount) {
        BlockPos playerPos = player.blockPosition();
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-radius, -radius, -radius), playerPos.offset(radius, radius, radius))) {
            if (found.size() >= maxCount) break;
            BlockState state = player.level.getBlockState(pos);
            if (state.getBlock().getRegistryName() != null && state.getBlock().getRegistryName().getPath().contains("ore")) {
                found.add(pos.immutable());
            }
        }
        return found;
    }
}