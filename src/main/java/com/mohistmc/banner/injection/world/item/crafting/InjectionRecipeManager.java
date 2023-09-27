package com.mohistmc.banner.injection.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

public interface InjectionRecipeManager {

    default void addRecipe(RecipeHolder<?> irecipe) {
    }

    default boolean removeRecipe(ResourceLocation mcKey) {
        return false;
    }

    default void clearRecipes() {
    }
}
