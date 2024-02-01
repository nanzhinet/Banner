package com.mohistmc.banner.mixin.bukkit.util;

import org.bukkit.craftbukkit.v1_20_R3.util.Versioning;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = Versioning.class, remap = false)
public class MixinVersioning {

    /**
     * @author wdog5
     * @reason banner things
     */
    @Overwrite
    public static String getBukkitVersion() {
        return "1.20.4-R0.1-SNAPSHOT";
    }
}
