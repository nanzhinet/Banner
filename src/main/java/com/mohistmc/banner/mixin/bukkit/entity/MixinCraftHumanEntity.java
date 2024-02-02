package com.mohistmc.banner.mixin.bukkit.entity;

import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftInventoryPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftHumanEntity.class, remap = false)
public abstract class MixinCraftHumanEntity extends CraftLivingEntity implements HumanEntity {

    @Shadow public abstract Player getHandle();

    @Shadow private CraftInventoryPlayer inventory;

    public MixinCraftHumanEntity(CraftServer server, LivingEntity entity) {
        super(server, entity);
    }

    @Inject(method = "getOpenInventory", at = @At("HEAD"))
    private void banner$capturePlayer(CallbackInfoReturnable<InventoryView> cir) {
        BukkitSnapshotCaptures.captureContainerOwner(this.getHandle());
    }

    @Inject(method = "getOpenInventory", at = @At("RETURN"))
    private void banner$resetPlayer(CallbackInfoReturnable<InventoryView> cir) {
        BukkitSnapshotCaptures.resetContainerOwner();
    }

    @Override
    public void setHandle(Entity entity) {
        super.setHandle(entity);
        this.inventory = new CraftInventoryPlayer(((Player) entity).getInventory());
    }
}
