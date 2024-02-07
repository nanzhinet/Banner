package com.mohistmc.banner.mixin.bukkit.inventory;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.bukkit.craftbukkit.v1_20_R3.inventory.RecipeIterator;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Map;

@Mixin(value = RecipeIterator.class, remap = false)
public class MixinRecipeIterator {

    @Shadow @Final private Iterator<Map.Entry<RecipeType<?>, Object2ObjectLinkedOpenHashMap<ResourceLocation, RecipeHolder<?>>>> recipes;
    @Shadow private Iterator<RecipeHolder<?>> current;

    /**
     * @author wdog5
     * @reason mod compat
     */
    @Overwrite
    public boolean hasNext() {
        if (this.current != null && this.current.hasNext()) {
            return true;
        } else if (this.recipes.hasNext()) {
            this.current = this.recipes.next().getValue().values().iterator();
            return this.hasNext();
        } else {
            return false;
        }
    }

    /**
     * @author wdog5
     * @reason mod compat
     */
    @Overwrite
    public Recipe next() {
        if (current == null || !current.hasNext()) {
            current = recipes.next().getValue().values().iterator();
            return next();
        }
        var recipe = current.next();
        try {
            return recipe.toBukkitRecipe();
        } catch (Throwable e) {
            throw new RuntimeException("Error converting recipe " + recipe.id(), e);
        }
    }
}
