package com.mohistmc.banner.mixin.bukkit.inventory;

import com.mohistmc.banner.bukkit.BukkitContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftInventoryView.class, remap = false)
public abstract class MixinCraftInventoryView extends InventoryView {

    @Shadow @Final @Mutable private CraftInventory viewing;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$validate(HumanEntity player, Inventory viewing, AbstractContainerMenu container, CallbackInfo ci) {
        if (container.slots.size() > this.countSlots()) {
            this.viewing = BukkitContainer.createInv(((CraftHumanEntity) player).getHandle(), container);
        }
    }
}
