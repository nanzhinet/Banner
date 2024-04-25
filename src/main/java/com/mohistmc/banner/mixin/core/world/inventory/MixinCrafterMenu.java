package com.mohistmc.banner.mixin.core.world.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftInventoryCrafter;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftInventoryView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrafterMenu.class)
public abstract class MixinCrafterMenu extends AbstractContainerMenu {


    @Shadow @Final private CraftingContainer container;
    @Shadow @Final private ResultContainer resultContainer;
    @Shadow @Final private Player player;

    private CraftInventoryView bukkitEntity;

    protected MixinCrafterMenu(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }

        CraftInventoryCrafter inventory = new CraftInventoryCrafter(this.container, this.resultContainer);
        bukkitEntity = new CraftInventoryView((((ServerPlayer) this.player).getBukkitEntity()), inventory, (CrafterMenu) (Object) this);
        return bukkitEntity;
    }

    @Inject(method = "stillValid", cancellable = true, at = @At("HEAD"))
    public void banner$unreachable(Player playerIn, CallbackInfoReturnable<Boolean> cir) {
        if (!bridge$checkReachable()) cir.setReturnValue(true);
    }
}
