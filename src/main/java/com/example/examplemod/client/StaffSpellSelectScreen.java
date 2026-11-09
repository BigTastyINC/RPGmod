package com.example.examplemod.client;

import com.example.examplemod.capability.ModCapabilities;
import com.example.examplemod.item.StaffItem;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.SelectStaffSpellPacket;
import com.example.examplemod.spells.Spell;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import java.util.Map;

public class StaffSpellSelectScreen extends Screen {

    private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation("examplemod", "textures/gui/stats_book.png");

    public StaffSpellSelectScreen() {
        super(new StringTextComponent("Выбор заклинания"));
    }

    @Override
    protected void init() {
        super.init();

        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack staff = player.getMainHandItem();
        if (!(staff.getItem() instanceof StaffItem)) {
            this.onClose();
            return;
        }

        CompoundNBT staffNbt = staff.getTag();
        String currentSpell = staffNbt != null ? staffNbt.getString("SelectedSpell") : "";

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
            int count = 0;
            for (Map.Entry<String, Integer> entry : data.getKnownSpells().entrySet()) {
                if (count >= 7) break;
                final String spellId = entry.getKey();
                Spell spell = Spell.byId(spellId);
                int lvl = entry.getValue();
                boolean isSelected = spellId.equals(currentSpell);

                String prefix = isSelected ? "§a✔ " : "§7";

                int bx = centerX - 120;
                int by = centerY - 70 + (count * 20);

                this.addButton(new Button(bx, by, 240, 18,
                        new StringTextComponent(prefix + spell.getName() + " §7(" + lvl + " ур.)"),
                        b -> {
                            NetworkHandler.INSTANCE.sendToServer(new SelectStaffSpellPacket(spellId));
                            this.onClose();
                        }));
                count++;
            }
        });

        this.addButton(new Button(centerX - 40, centerY + 95, 80, 20,
                new StringTextComponent("Закрыть"), b -> this.onClose()));
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        Minecraft.getInstance().getTextureManager().bind(BOOK_TEXTURE);
        blit(ms, centerX - 256, centerY - 128, 0, 0, 512, 256, 512, 256);

        drawCenteredString(ms, this.font, "§6Выбор заклинания для посоха", centerX, centerY - 95, 0xFFFFFF);

        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(data -> {
                if (data.getKnownSpells().isEmpty()) {
                    drawCenteredString(ms, this.font, "§7Вы не знаете ни одного заклинания...",
                            centerX, centerY - 40, 0xFFFFFF);
                }
            });
        }

        super.render(ms, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}