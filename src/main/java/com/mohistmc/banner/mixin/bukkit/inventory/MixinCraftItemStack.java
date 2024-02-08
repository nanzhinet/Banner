package com.mohistmc.banner.mixin.bukkit.inventory;

import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftItemStack.class, remap = false)
public abstract class MixinCraftItemStack {

    @Shadow
    static Material getType(ItemStack item) {
        return null;
    }

    @Inject(method = "getItemMeta(Lnet/minecraft/world/item/ItemStack;)Lorg/bukkit/inventory/meta/ItemMeta;",
            at = @At("HEAD"), cancellable = true)
    private static void banner$checkItemMeta(ItemStack item, CallbackInfoReturnable<ItemMeta> cir) {
        if (item.getTag() == null) {
            cir.setReturnValue(CraftItemFactory.instance().getItemMeta(getType(item)));
        }
    }
}
