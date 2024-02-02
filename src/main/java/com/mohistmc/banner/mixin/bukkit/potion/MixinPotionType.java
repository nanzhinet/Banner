package com.mohistmc.banner.mixin.bukkit.potion;

import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PotionType.class, remap = false)
public class MixinPotionType {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/bukkit/NamespacedKey;minecraft(Ljava/lang/String;)Lorg/bukkit/NamespacedKey;"))
    private NamespacedKey banner$nsFromString(String key) {
        return NamespacedKey.fromString(key);
    }
}
