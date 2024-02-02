package com.mohistmc.banner.mixin.bukkit.block;

import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CraftBlockState.class)
public class MixinCraftBlockState {

    @Shadow(remap = false) protected BlockState data;

    @Override
    public String toString() {
        return this.data.toString();
    }
}
