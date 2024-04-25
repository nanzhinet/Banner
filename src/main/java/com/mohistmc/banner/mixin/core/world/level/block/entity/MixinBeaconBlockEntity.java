package com.mohistmc.banner.mixin.core.world.level.block.entity;

import com.mohistmc.banner.injection.world.level.block.entity.InjectionBeaconBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_20_R4.potion.CraftPotionUtil;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
public abstract class MixinBeaconBlockEntity extends BlockEntity implements InjectionBeaconBlockEntity{

    @Shadow public int levels;

    @Shadow @Nullable Holder<MobEffect> secondaryPower;

    @Shadow @Nullable Holder<MobEffect> primaryPower;

    public MixinBeaconBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    public void banner$level(CompoundTag compoundTag, Provider provider, CallbackInfo ci) {
        this.levels = compoundTag.getInt("Levels");
    }

    @Override
    public PotionEffect getPrimaryEffect() {
        return (this.primaryPower != null) ? CraftPotionUtil.toBukkit(new MobEffectInstance(this.primaryPower, getLevel(this.levels), getAmplification(levels, primaryPower, secondaryPower), true, true)) : null;
    }

    @Override
    public PotionEffect getSecondaryEffect() {
        return (hasSecondaryEffect(levels, primaryPower, secondaryPower)) ? CraftPotionUtil.toBukkit(new MobEffectInstance(this.secondaryPower, getLevel(this.levels), getAmplification(levels, primaryPower, secondaryPower), true, true)) : null;
    }

    private static boolean hasSecondaryEffect(int i, @Nullable Holder<MobEffect> mobeffectlist, @Nullable Holder<MobEffect> mobeffectlist1) {
        {
            if (i >= 4 && mobeffectlist != mobeffectlist1 && mobeffectlist1 != null) {
                return true;
            }

            return false;
        }
    }


    // CraftBukkit start - split into components
    private static byte getAmplification(int i, @Nullable Holder<MobEffect> mobeffectlist, @Nullable Holder<MobEffect> mobeffectlist1) {
        {
            byte b0 = 0;

            if (i >= 4 && mobeffectlist == mobeffectlist1) {
                b0 = 1;
            }

            return b0;
        }
    }

    private static int getLevel(int i) {
        {
            int j = (9 + i * 2) * 20;
            return j;
        }
    }
}
