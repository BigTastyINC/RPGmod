package com.example.examplemod.item;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;

public class ModArmorItem extends ArmorItem {

    public ModArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties properties) {
        super(material, slot, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        // Берем имя материала (например, "silver" из "examplemod:silver")
        String matName = this.getMaterial().getName().split(":")[1];

        // Если это штаны (LEGS), используем layer_2, для остального layer_1
        if (slot == EquipmentSlotType.LEGS) {
            return "examplemod:textures/models/armor/" + matName + "_layer_2.png";
        } else {
            return "examplemod:textures/models/armor/" + matName + "_layer_1.png";
        }
    }
}