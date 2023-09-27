package com.mohistmc.banner.mixin.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.mohistmc.banner.injection.world.item.crafting.InjectionRecipeManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.units.qual.C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class MixinRecipeManager implements InjectionRecipeManager {

    @Shadow private Map<ResourceLocation, RecipeHolder<?>> byName;

    @Shadow public Map<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>> recipes;

    @Shadow protected abstract <C extends Container, T extends Recipe<C>> Map<ResourceLocation, RecipeHolder<T>>byType(RecipeType<T> recipeType);

    /**
     * @author wdog5
     * @reason bukkit current recipe
     */
    @Overwrite
    public <C extends Container, T extends Recipe<C>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> recipeTypeIn, C inventoryIn, Level worldIn) {
        Optional<RecipeHolder<T>> optional = this.byType(recipeTypeIn).values().stream().filter((recipe) -> {
            return recipe.value().matches(inventoryIn, worldIn);
        }).findFirst();
        inventoryIn.setCurrentRecipe(optional.orElse(null));
        return optional;
    }

    @Override
    public void addRecipe(RecipeHolder<?> recipe) {
        if (this.recipes instanceof ImmutableMap) {
            this.recipes = new HashMap<>(recipes);
        }
        if (this.byName instanceof ImmutableMap) {
            this.byName = new HashMap<>(byName);
        }
        Map<ResourceLocation, RecipeHolder<?>> original = this.recipes.get(recipe.value().getType());
        Object2ObjectLinkedOpenHashMap<ResourceLocation, RecipeHolder<?>> map;
        if (!(original instanceof Object2ObjectLinkedOpenHashMap)) {
            Object2ObjectLinkedOpenHashMap<ResourceLocation, RecipeHolder<?>> hashMap = new Object2ObjectLinkedOpenHashMap<>();
            hashMap.putAll(original);
            this.recipes.put(recipe.value().getType(), hashMap);
            map = hashMap;
        } else {
            map = ((Object2ObjectLinkedOpenHashMap<ResourceLocation, RecipeHolder<?>>) original);
        }

        if (this.byName.containsKey(recipe.id()) || map.containsKey(recipe.id())) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + recipe.id());
        } else {
            map.putAndMoveToFirst(recipe.id(), recipe);
            this.byName.put(recipe.id(), recipe);
        }
    }

    @Override
    public boolean removeRecipe(ResourceLocation mcKey) {
        for (var recipes : recipes.values()) {
            recipes.remove(mcKey);
        }
        return byName.remove(mcKey) != null;
    }

    @Override
    public void clearRecipes() {
        this.recipes = new HashMap<>();
        for (RecipeType<?> type : BuiltInRegistries.RECIPE_TYPE) {
            this.recipes.put(type, new Object2ObjectLinkedOpenHashMap<>());
        }
        this.byName = new HashMap<>();
    }

}
