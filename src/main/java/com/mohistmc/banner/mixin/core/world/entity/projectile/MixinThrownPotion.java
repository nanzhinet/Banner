package com.mohistmc.banner.mixin.core.world.entity.projectile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R4.event.CraftEventFactory;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ThrownPotion.class)
public abstract class MixinThrownPotion extends ThrowableItemProjectile {


    @Unique
    private transient HitResult banner$hitResult;

    public MixinThrownPotion(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "onHit", at = @At(value = "HEAD"))
    private boolean banner$callEvent(HitResult hitResult) {
        banner$hitResult = hitResult;
        return false;
    }

    @Inject(method = "onHit", at = @At("RETURN"))
    private void banner$resetResult(HitResult p_37543_, CallbackInfo ci) {
        banner$hitResult = null;
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    private void applySplash(Iterable<MobEffectInstance> iterable, @Nullable Entity entity) {
        AABB axisalignedbb = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, axisalignedbb);
        Map<org.bukkit.entity.LivingEntity, Double> affected = new HashMap<>();
        if (!list.isEmpty()) {
            Entity entity2 = this.getEffectSource();
            for (LivingEntity entityliving : list) {
                if (entityliving.isAffectedByPotions()) {
                    double d0 = this.distanceToSqr(entityliving);
                    if (d0 < 16.0D) {
                        double d1;

                        if (entityliving == entity) {
                            d1 = 1.0D;
                        } else {
                            d1 = 1.0D - Math.sqrt(d0) / 4.0D;
                        }
                        affected.put((org.bukkit.entity.LivingEntity) entityliving.getBukkitEntity(), d1);
                    }
                }
            }
        }
        PotionSplashEvent event = CraftEventFactory.callPotionSplashEvent((ThrownPotion) (Object) this, banner$hitResult, affected);
        if (!event.isCancelled() && list != null && !list.isEmpty()) {
            Entity entity1 = this.getEffectSource();
            for (org.bukkit.entity.LivingEntity victim : event.getAffectedEntities()) {
                if (!(victim instanceof CraftLivingEntity)) {
                    continue;
                }
                LivingEntity entityliving2 = ((CraftLivingEntity) victim).getHandle();
                double d2 = event.getIntensity(victim);
                for (MobEffectInstance mobeffect : iterable) {
                    Holder<MobEffect> mobeffectlist = mobeffect.getEffect();
                    if (!this.level().bridge$pvpMode() && this.getOwner() instanceof ServerPlayer && entityliving2 instanceof ServerPlayer && entityliving2 != this.getOwner()) {
                        if (mobeffectlist == MobEffects.MOVEMENT_SLOWDOWN || mobeffectlist == MobEffects.DIG_SLOWDOWN || mobeffectlist == MobEffects.HARM || mobeffectlist == MobEffects.BLINDNESS
                                || mobeffectlist == MobEffects.HUNGER || mobeffectlist == MobEffects.WEAKNESS || mobeffectlist == MobEffects.POISON) {
                            continue;
                        }
                    }
                    if (mobeffectlist.value().isInstantenous()) {
                        mobeffectlist.value().applyInstantenousEffect((ThrownPotion) (Object) this, this.getOwner(), entityliving2, mobeffect.getAmplifier(), d2);
                    } else {
                        int i = mobeffect.mapDuration((ix) -> {
                            return (int)(d2 * (double)ix + 0.5);
                        });
                        MobEffectInstance mobEffectInstance2 = new MobEffectInstance(mobeffectlist, i, mobeffect.getAmplifier(), mobeffect.isAmbient(), mobeffect.isVisible());
                        if (!mobEffectInstance2.endsWithin(20)) {
                            entityliving2.pushEffectCause(EntityPotionEffectEvent.Cause.POTION_SPLASH);
                            entityliving2.addEffect(mobEffectInstance2, entity1);
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "makeAreaOfEffectCloud", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$makeCloud(PotionContents potionContents, CallbackInfo ci, AreaEffectCloud areaEffectCloud, LivingEntity livingEntity, Entity var4) {
        LingeringPotionSplashEvent event = CraftEventFactory.callLingeringPotionSplashEvent((ThrownPotion) (Object) this, banner$hitResult, areaEffectCloud);
        if (event.isCancelled() || areaEffectCloud.isRemoved()) {
            ci.cancel();
            areaEffectCloud.discard();
        }
    }

    @Inject(method = "dowseFire", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"))
    private void banner$entityChangeBlock(BlockPos pos, CallbackInfo ci) {
        if (!CraftEventFactory.callEntityChangeBlockEvent((ThrownPotion) (Object) this, pos, Blocks.AIR.defaultBlockState())) {
            ci.cancel();
        }
    }

    @Inject(method = "dowseFire", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;levelEvent(Lnet/minecraft/world/entity/player/Player;ILnet/minecraft/core/BlockPos;I)V"))
    private void banner$entityChangeBlock2(BlockPos pos, CallbackInfo ci, BlockState state) {
        if (!CraftEventFactory.callEntityChangeBlockEvent((ThrownPotion) (Object) this, pos, state.setValue(CampfireBlock.LIT, false))) {
            ci.cancel();
        }
    }

    @Inject(method = "dowseFire", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/AbstractCandleBlock;extinguish(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)V"))
    private void banner$entityChangeBlock3(BlockPos pos, CallbackInfo ci, BlockState state) {
        if (!CraftEventFactory.callEntityChangeBlockEvent((ThrownPotion) (Object) this, pos, state.setValue(AbstractCandleBlock.LIT, false))) {
            ci.cancel();
        }
    }
}
