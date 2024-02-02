package com.mohistmc.banner.mixin.bukkit.util;

import net.minecraft.world.entity.MobCategory;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftSpawnCategory;
import org.bukkit.entity.SpawnCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftSpawnCategory.class, remap = false)
public class MixinCraftSpawnCategory {

    /**
     * @author wdog5
     * @reason mod compat
     */
    @Overwrite
    public static boolean isValidForLimits(SpawnCategory spawnCategory) {
        return spawnCategory != null && spawnCategory.ordinal() < SpawnCategory.MISC.ordinal();
    }

    @Inject(method = "toBukkit", cancellable = true, at = @At(value = "NEW", args = "class=java/lang/UnsupportedOperationException"))
    private static void banner$modToBukkit(MobCategory mobCategory, CallbackInfoReturnable<SpawnCategory> cir) {
        cir.setReturnValue(SpawnCategory.valueOf(mobCategory.name()));
    }

    @Inject(method = "toNMS", cancellable = true, at = @At(value = "NEW", args = "class=java/lang/UnsupportedOperationException"))
    private static void banner$bukkitToMod(SpawnCategory spawnCategory, CallbackInfoReturnable<MobCategory> cir) {
        cir.setReturnValue(MobCategory.valueOf(spawnCategory.name()));
    }
}
