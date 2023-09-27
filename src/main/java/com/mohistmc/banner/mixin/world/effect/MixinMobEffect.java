package com.mohistmc.banner.mixin.world.effect;

import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;

// Banner TODO
@Mixin(MobEffect.class)
public class MixinMobEffect {

    /*
    @Inject(method = "applyEffectTick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/entity/LivingEntity;heal(F)V"))
    public void banner$healReason1(LivingEntity livingEntity, int amplifier, CallbackInfo ci) {
         livingEntity.pushHealReason(EntityRegainHealthEvent.RegainReason.MAGIC_REGEN);
    }

    @Inject(method = "applyEffectTick", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/entity/LivingEntity;heal(F)V"))
    public void banner$healReason2(LivingEntity livingEntity, int amplifier, CallbackInfo ci) {
         livingEntity.pushHealReason(EntityRegainHealthEvent.RegainReason.MAGIC);
    }

    @Inject(method = "applyInstantenousEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;heal(F)V"))
    public void banner$healReason3(Entity source, Entity indirectSource, LivingEntity livingEntity, int amplifier, double health, CallbackInfo ci) {
        livingEntity.pushHealReason(EntityRegainHealthEvent.RegainReason.MAGIC);
    }

    @Redirect(method = "applyEffectTick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/damagesource/DamageSources;magic()Lnet/minecraft/world/damagesource/DamageSource;"))
    private DamageSource banner$redirectPoison(DamageSources instance) {
        return  instance.bridge$poison();
    }

    @Redirect(method = "applyEffectTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(IF)V"))
    public void banner$foodLevelChange(FoodData foodStats, int foodLevelIn, float foodSaturationModifier, LivingEntity livingEntity, int amplifier) {
        Player playerEntity = ((Player) livingEntity);
        int oldFoodLevel = playerEntity.getFoodData().getFoodLevel();
        FoodLevelChangeEvent event = CraftEventFactory.callFoodLevelChangeEvent(playerEntity, foodLevelIn + oldFoodLevel);
        if (!event.isCancelled()) {
            playerEntity.getFoodData().eat(event.getFoodLevel() - oldFoodLevel, foodSaturationModifier);
        }
        ((ServerPlayer) playerEntity).connection.send(new ClientboundSetHealthPacket(((ServerPlayer) playerEntity).getBukkitEntity().getScaledHealth(),
                playerEntity.getFoodData().getFoodLevel(), playerEntity.getFoodData().getSaturationLevel()));

    }*/
}
