package com.mohistmc.banner.mixin.world.item;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_20_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LeadItem.class)
@SuppressWarnings("deprecation")
public abstract class MixinLeadItem {

    @Shadow
    public static InteractionResult bindPlayerMobs(Player player, Level level, BlockPos pos) {
        return null;
    }

    @Unique
    private static AtomicReference<InteractionHand> banner$hand = new AtomicReference<>(InteractionHand.MAIN_HAND);

    @Inject(method = "bindPlayerMobs",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/decoration/LeashFenceKnotEntity;playPlacementSound()V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void banner$bindPlayerMobs(Player player, Level level, BlockPos pos, CallbackInfoReturnable<InteractionResult> cir, LeashFenceKnotEntity leashFenceKnotEntity, boolean bl, double d, int i, int j, int k, List list, Iterator var11, Mob mob) {
        // CraftBukkit start - fire HangingPlaceEvent
        org.bukkit.inventory.EquipmentSlot hand = CraftEquipmentSlot.getHand(banner$hand.get());
        HangingPlaceEvent event = new HangingPlaceEvent((org.bukkit.entity.Hanging) leashFenceKnotEntity.getBukkitEntity(), player != null ? (org.bukkit.entity.Player) player.getBukkitEntity() : null, level.getWorld().getBlockAt(i, j, k), org.bukkit.block.BlockFace.SELF, hand);
        level.getCraftServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            leashFenceKnotEntity.discard();
            cir.setReturnValue(InteractionResult.PASS);
        }
        // CraftBukkit end
    }

    @Inject(method = "bindPlayerMobs", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Mob;setLeashedTo(Lnet/minecraft/world/entity/Entity;Z)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void banner$continueSet(Player player, Level level, BlockPos pos, CallbackInfoReturnable<InteractionResult> cir, LeashFenceKnotEntity leashFenceKnotEntity, boolean bl, double d, int i, int j, int k, List list, Iterator var11, Mob mob) {
        // CraftBukkit start
        if (player != null && CraftEventFactory.callPlayerLeashEntityEvent(mob, leashFenceKnotEntity, player, banner$hand.get()).isCancelled()) {
            cir.cancel();
        }
    }

    @Unique
    private static InteractionResult bindPlayerMobs(Player entityhuman, Level world, BlockPos blockposition, InteractionHand enumhand) { // CraftBukkit - Add EnumHand
       banner$hand.set(enumhand);
       return bindPlayerMobs(entityhuman, world, blockposition);
    }
}
