package com.mohistmc.banner.mixin.world.item.crafting;

import com.mohistmc.banner.injection.world.item.crafting.InjectionRecipe;
import com.mohistmc.banner.recipe.BannerModdedRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author Mgazul by MohistMC
 * @date 2023/9/27 21:56:57
 */
@Mixin(net.minecraft.world.item.crafting.Recipe.class)
public interface MixinRecipe extends InjectionRecipe {

    @Override
    default Recipe toBukkitRecipe(NamespacedKey id) {
        return new BannerModdedRecipe(id, (net.minecraft.world.item.crafting.Recipe<?>) this);
    }
}
