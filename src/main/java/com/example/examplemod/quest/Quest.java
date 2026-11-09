package com.example.examplemod.quest;

import com.example.examplemod.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.UUID;

public class Quest {
    public enum Type { KILL_MOB, KILL_BOSS, DELIVER_ITEM }

    private UUID id;
    private Type type;
    private String target;
    private int minLevel;
    private int minRarity;
    private int amountRequired;
    private int progress;
    private int rewardCoins;
    private int rewardXp;

    public Quest(Type type, String target, int minLevel, int minRarity, int amountRequired, int rewardCoins, int rewardXp) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.target = target;
        this.minLevel = minLevel;
        this.minRarity = minRarity;
        this.amountRequired = amountRequired;
        this.progress = 0;
        this.rewardCoins = rewardCoins;
        this.rewardXp = rewardXp;
    }

    public UUID getId() { return id; }
    public Type getType() { return type; }
    public String getTarget() { return target; }
    public int getMinLevel() { return minLevel; }
    public int getMinRarity() { return minRarity; }
    public int getAmountRequired() { return amountRequired; }
    public int getProgress() { return progress; }
    public int getRewardCoins() { return rewardCoins; }
    public int getRewardXp() { return rewardXp; }

    public void addProgress(int amount) { this.progress = Math.min(amountRequired, this.progress + amount); }
    public void setProgress(int progress) { this.progress = progress; }
    public boolean isCompleted() { return progress >= amountRequired; }

    // НОВОЕ: Возвращаем ITextComponent с переводами и правильным цветом редкости
    public ITextComponent getDescription() {
        ITextComponent targetComp = new StringTextComponent(target); // fallback
        if (target.contains(":")) {
            if (type == Type.DELIVER_ITEM) {
                targetComp = new TranslationTextComponent("item." + target.replace(":", "."));
            } else {
                targetComp = new TranslationTextComponent("entity." + target.replace(":", "."));
            }
        }

        StringTextComponent result = new StringTextComponent("");
        switch (type) {
            case KILL_MOB:
                result.append(new StringTextComponent("Убейте " + amountRequired + " шт. "));
                result.append(targetComp);
                result.append(new StringTextComponent(" ур. " + minLevel + "+"));
                break;
            case KILL_BOSS:
                result.append(new StringTextComponent("Убейте Лорда "));
                result.append(targetComp);
                break;
            case DELIVER_ITEM:
                Rarity r = Rarity.values()[Math.min(minRarity, 4)];
                result.append(new StringTextComponent("Принесите " + amountRequired + " шт. "));
                result.append(targetComp);
                result.append(new StringTextComponent(" редкости " + r.getColor() + r.getName() + "+"));
                break;
            default:
                result.append(new StringTextComponent("Неизвестный квест"));
        }
        return result;
    }

    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putUUID("id", id);
        nbt.putInt("type", type.ordinal());
        nbt.putString("target", target);
        nbt.putInt("minLevel", minLevel);
        nbt.putInt("minRarity", minRarity);
        nbt.putInt("amountRequired", amountRequired);
        nbt.putInt("progress", progress);
        nbt.putInt("rewardCoins", rewardCoins);
        nbt.putInt("rewardXp", rewardXp);
        return nbt;
    }

    public static Quest deserializeNBT(CompoundNBT nbt) {
        Quest q = new Quest(
                Type.values()[nbt.getInt("type")],
                nbt.getString("target"),
                nbt.getInt("minLevel"),
                nbt.getInt("minRarity"),
                nbt.getInt("amountRequired"),
                nbt.getInt("rewardCoins"),
                nbt.getInt("rewardXp")
        );
        q.id = nbt.getUUID("id");
        q.progress = nbt.getInt("progress");
        return q;
    }
}